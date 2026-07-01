package com.mall.wallet.payout;

import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapter;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.chain.adapter.PayoutCommand;
import com.mall.wallet.chain.mapper.CoinConfigMapper;
import com.mall.wallet.chain.model.entity.CoinConfig;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.security.SecretCipher;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * 出款签名边界：把"用某地址私钥从链上转出资产"收敛为单一职责服务，同时服务提现出账与归集。
 * <p>
 * 职责（且仅此）：选定出款地址(热钱包/gas钱包/用户地址) → 即时解密私钥（{@link SecretCipher}，<strong>KMS 迁移点</strong>，
 * 明文私钥仅在方法栈内存活）→ 解析币种合约与链上精度（coin_config 真相源）→ 构造 {@link PayoutCommand}
 * 交对应链适配器签名广播。不碰订单、不碰账务、不做状态机——那是 WithdrawService / CollectService 的职责。
 * <p>
 * 能力：
 * <ul>
 *   <li>{@link #payout}：提现出款（from=出款热钱包，固定金额）；</li>
 *   <li>{@link #collectAll}：归集（from=用户充值地址，转全部 USDT 到出款热钱包）；</li>
 *   <li>{@link #ensureGas}：喂 gas（from=gas 供给钱包，按需向用户地址代付原生币）；</li>
 *   <li>{@link #usdtBalance}：只读查询 USDT 余额（归集阈值决策）。</li>
 * </ul>
 */
@Slf4j
@Service
public class PayoutService {

    /** 归集与只读余额查询的默认币种。 */
    private static final String DEFAULT_COIN = "USDT";

    @Resource
    private HotWalletProperties hotWalletProperties;

    @Resource
    private GasStationProperties gasStationProperties;

    @Resource
    private ChainAdapterRegistry adapterRegistry;

    @Resource
    private CoinConfigMapper coinConfigMapper;

    @Resource
    private SecretCipher secretCipher;

    /**
     * 从本链热钱包转出 {@code amount} 个 {@code coin} 到 {@code toAddress}，返回出款地址与交易哈希。
     * 私钥即时解密、用后即弃；金额精度由 coin_config.decimals 决定，适配器内按之归一化为链上整数。
     */
    public PayoutResult payout(ChainCode chainCode, String coin, String toAddress, BigDecimal amount) throws BizException {
        HotWalletProperties.ChainHotWallet hot = hotWalletProperties.getChains().get(chainCode.name());
        if (hot == null || !StringUtils.hasText(hot.getAddress()) || !StringUtils.hasText(hot.getPrivateKeyCipher())) {
            throw new BizException(WalletRespCodeEnum.HOT_WALLET_NOT_CONFIGURED, new Object[]{chainCode.name()});
        }

        CoinConfig coinConfig = findCoin(chainCode, coin);
        if (coinConfig == null) {
            throw new BizException(WalletRespCodeEnum.COIN_CONFIG_MISSING, new Object[]{chainCode.name() + ":" + coin});
        }

        // KMS 迁移点：当前用 SecretCipher 解密密文私钥；明文仅在本栈内存活，交给适配器后不再持有引用
        String privateKey = secretCipher.decrypt(hot.getPrivateKeyCipher());

        PayoutCommand command = PayoutCommand.builder()
                .chainCode(chainCode)
                .fromAddress(hot.getAddress())
                .privateKeyHex(privateKey)
                .toAddress(toAddress)
                .amount(amount)
                .contractAddress(coinConfig.getContractAddress())
                .decimals(coinConfig.getDecimals() == null ? 0 : coinConfig.getDecimals())
                .build();

        String txHash = adapterRegistry.get(chainCode).transferOut(command);
        log.info("出款广播成功 chain={} coin={} to={} amount={} tx={}", chainCode, coin, toAddress, amount, txHash);
        return new PayoutResult(hot.getAddress(), txHash);
    }

    /**
     * 归集：把 {@code fromAddress} 上的<strong>全部 USDT</strong> 转入本链出款热钱包，返回归集结果；
     * 余额为 0 返回 {@link Optional#empty()}（无可归集）——这是「重复归集第二次必为空」的幂等基石①。
     * <p>
     * 与 {@link #payout} 同为链上代币转出，差异在于：源地址是用户充值地址（私钥来自其加密落库的密文，本方法即时解密），
     * 且转出额取「调用时刻实时全余额」而非固定申请额。私钥明文仅在本方法栈内存活，用后即弃。
     *
     * @param encPrivKeyCipher 用户充值地址私钥密文（来自 user_deposit_address.enc_priv_key）
     */
    public Optional<CollectPayout> collectAll(ChainCode chainCode, String fromAddress, String encPrivKeyCipher) throws BizException {
        HotWalletProperties.ChainHotWallet hot = hotWalletProperties.getChains().get(chainCode.name());
        if (hot == null || !StringUtils.hasText(hot.getAddress())) {
            throw new BizException(WalletRespCodeEnum.HOT_WALLET_NOT_CONFIGURED, new Object[]{chainCode.name()});
        }
        CoinConfig coinConfig = findCoin(chainCode, DEFAULT_COIN);
        if (coinConfig == null) {
            throw new BizException(WalletRespCodeEnum.COIN_CONFIG_MISSING, new Object[]{chainCode.name() + ":" + DEFAULT_COIN});
        }
        ChainAdapter adapter = adapterRegistry.get(chainCode);

        // 幂等基石①：归集「实时全部余额」。余额为 0 即无可归集，直接跳过（重复归集第二次必空，不会重复转钱）
        BigInteger balanceRaw = adapter.tokenBalanceRaw(fromAddress, coinConfig.getContractAddress());
        if (balanceRaw.signum() <= 0) {
            return Optional.empty();
        }
        int decimals = coinConfig.getDecimals() == null ? 0 : coinConfig.getDecimals();
        // raw → 人类可读；适配器内部再 ×10^decimals 归一化回 raw，整数往返无损
        BigDecimal amount = new BigDecimal(balanceRaw).movePointLeft(decimals);

        // KMS 迁移点：即时解密用户地址私钥；明文仅本栈内存活，交适配器后不再持有
        String privateKey = secretCipher.decrypt(encPrivKeyCipher);
        PayoutCommand command = PayoutCommand.builder()
                .chainCode(chainCode)
                .fromAddress(fromAddress)
                .privateKeyHex(privateKey)
                .toAddress(hot.getAddress())
                .amount(amount)
                .contractAddress(coinConfig.getContractAddress())
                .decimals(decimals)
                .build();

        String txHash = adapter.transferOut(command);
        log.info("归集广播成功 chain={} from={} to={} amount={} tx={}", chainCode, fromAddress, hot.getAddress(), amount, txHash);
        return Optional.of(new CollectPayout(hot.getAddress(), amount, txHash));
    }

    /**
     * 喂 gas：若 {@code address} 原生币余额低于阈值 {@code minNative}，则从本链 gas 供给钱包代付 {@code fundAmount}
     * 原生币，返回 gas 交易哈希；已达阈值返回 {@link Optional#empty()}（跳过，不浪费 gas）——幂等基石②。
     * <p>
     * 用户充值地址通常无原生币、无法自付代币转账手续费，故归集前需先用这笔小额原生币把地址「点火」。
     * gas 钱包私钥即时解密、用后即弃。原生币精度由适配器经 {@link ChainAdapter#nativeDecimals()} 提供。
     */
    public Optional<String> ensureGas(ChainCode chainCode, String address) throws BizException {
        GasStationProperties.ChainGasStation station = gasStationProperties.station(chainCode.name());
        if (station == null || !StringUtils.hasText(station.getAddress()) || !StringUtils.hasText(station.getPrivateKeyCipher())) {
            throw new BizException(WalletRespCodeEnum.GAS_STATION_NOT_CONFIGURED, new Object[]{chainCode.name()});
        }
        ChainAdapter adapter = adapterRegistry.get(chainCode);
        int nativeDecimals = adapter.nativeDecimals();
        BigInteger nativeRaw = adapter.nativeBalanceRaw(address);
        // 阈值人类可读 → 链上最小单位整数，与 nativeBalanceRaw 同量纲比较（截断取整作阈值足矣）
        BigInteger minNativeRaw = station.getMinNative().movePointRight(nativeDecimals).toBigInteger();
        if (nativeRaw.compareTo(minNativeRaw) >= 0) {
            return Optional.empty();
        }

        String privateKey = secretCipher.decrypt(station.getPrivateKeyCipher());
        // 原生币转账：contractAddress 留空，decimals 由适配器按本链原生币精度内部决定
        PayoutCommand command = PayoutCommand.builder()
                .chainCode(chainCode)
                .fromAddress(station.getAddress())
                .privateKeyHex(privateKey)
                .toAddress(address)
                .amount(station.getFundAmount())
                .contractAddress(null)
                .decimals(0)
                .build();

        String txHash = adapter.transferNative(command);
        log.info("gas 代付广播成功 chain={} to={} amount={} tx={}", chainCode, address, station.getFundAmount(), txHash);
        return Optional.of(txHash);
    }

    /**
     * 只读查询某地址的 USDT 余额（人类可读单位），供归集编排创建订单时做余额快照与阈值决策。不涉及私钥。
     */
    public BigDecimal usdtBalance(ChainCode chainCode, String address) throws BizException {
        CoinConfig coinConfig = findCoin(chainCode, DEFAULT_COIN);
        if (coinConfig == null) {
            throw new BizException(WalletRespCodeEnum.COIN_CONFIG_MISSING, new Object[]{chainCode.name() + ":" + DEFAULT_COIN});
        }
        BigInteger balanceRaw = adapterRegistry.get(chainCode).tokenBalanceRaw(address, coinConfig.getContractAddress());
        int decimals = coinConfig.getDecimals() == null ? 0 : coinConfig.getDecimals();
        return new BigDecimal(balanceRaw).movePointLeft(decimals);
    }

    private CoinConfig findCoin(ChainCode chainCode, String coin) {
        return coinConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(CoinConfig.class)
                .eq(CoinConfig::getChainCode, chainCode.name())
                .eq(CoinConfig::getSymbol, coin)
                .eq(CoinConfig::getEnabled, 1));
    }
}