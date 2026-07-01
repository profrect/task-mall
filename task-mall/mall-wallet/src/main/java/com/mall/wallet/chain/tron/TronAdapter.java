package com.mall.wallet.chain.tron;

import com.google.protobuf.ByteString;
import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapter;
import com.mall.wallet.chain.adapter.DepositEvent;
import com.mall.wallet.chain.adapter.KeyMaterial;
import com.mall.wallet.chain.adapter.PayoutCommand;
import com.mall.wallet.enums.WalletRespCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TRON 链适配器。
 * <p>
 * 职责边界：
 * - 充值收款地址 = trident 即时生成的 SECP256K1 密钥对（Option D）；私钥经 KeyMaterial 带出，由地址分配层加密落库。
 * - 扫块 = TronGrid 只读查询 USDT-TRC20 转入 + 确认数计算；幂等去重 (chain,txHash,logIndex) 在编排层处理。
 * - 出款 = trident gRPC(ApiWrapper) 用给定私钥签名 TRC20 transfer 并广播；服务提现与归集。
 *   金额走底层 triggerCall + ABI uint256(BigInteger) 精确表达（Trc20Contract.transfer 的 long 入参无法表达小数）。
 * - 确认数 = 复用 TronGrid REST（当前链高 - 交易区块高 + 1）。
 */
@Slf4j
@Component
public class TronAdapter implements ChainAdapter {

    /** 原生币(TRX)精度恒为 6 位(sun)；归集喂 gas 时按此把人类可读金额归一化为 sun。 */
    private static final int NATIVE_DECIMALS = 6;

    private final TronGridClient client;
    private final TronProperties properties;

    public TronAdapter(TronGridClient client, TronProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ChainCode chainCode() {
        return ChainCode.TRON;
    }

    @Override
    public KeyMaterial newDepositAddress() throws BizException {
        try {
            KeyPair keyPair = KeyPair.generate();
            // toBase58CheckAddress(): T 开头地址；toPrivateKey(): hex 私钥（仅在内存流转，落库前必经 SecretCipher 加密）
            return new KeyMaterial(keyPair.toBase58CheckAddress(), keyPair.toPrivateKey());
        } catch (Exception e) {
            log.error("TRON 地址生成失败", e);
            throw new BizException(WalletRespCodeEnum.ADDRESS_DERIVE_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public List<DepositEvent> scanDeposits(Collection<String> watchAddresses) throws BizException {
        if (watchAddresses == null || watchAddresses.isEmpty()) {
            return List.of();
        }
        try {
            long nowBlock = client.getNowBlockNumber();
            List<DepositEvent> events = new ArrayList<>();
            for (String address : watchAddresses) {
                for (TronGridClient.Trc20Transfer t : client.getInboundUsdtTransfers(address)) {
                    long txBlock = client.getTxBlockNumber(t.txId());
                    long confirmations = txBlock > 0 ? Math.max(0, nowBlock - txBlock + 1) : 0;
                    events.add(DepositEvent.builder()
                            .chainCode(ChainCode.TRON)
                            .contractAddress(t.contractAddress())
                            .txHash(t.txId())
                            // TronGrid trc20 端点不暴露日志序号；一笔转账即一条记录，logIndex 取 0 已足够唯一
                            .logIndex(0)
                            .fromAddress(t.from())
                            .toAddress(t.to())
                            .amountRaw(t.valueRaw())
                            .blockHeight(txBlock)
                            .confirmations(confirmations)
                            .build());
                }
            }
            return events;
        } catch (Exception e) {
            log.error("TRON 扫块失败", e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public String transferOut(PayoutCommand cmd) throws BizException {
        if (!StringUtils.hasText(cmd.contractAddress())) {
            throw new BizException(WalletRespCodeEnum.COIN_CONFIG_MISSING, new Object[]{"TRON USDT contract"});
        }
        BigInteger rawAmount;
        try {
            // 归一化为链上整数（uint256）；标度超出 decimals 直接失败，绝不静默舍入
            rawAmount = cmd.amount().movePointRight(cmd.decimals()).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new BizException(WalletRespCodeEnum.INVALID_AMOUNT, new Object[]{cmd.decimals()});
        }

        ApiWrapper wrapper = null;
        try {
            wrapper = newWrapper(cmd.privateKeyHex());
            // ABI: transfer(address,uint256) —— uint256 用 BigInteger 精确承载链上整数金额
            List<Type> params = new ArrayList<>();
            params.add(new Address(cmd.toAddress()));
            params.add(new Uint256(rawAmount));
            Function function = new Function("transfer", params, Collections.emptyList());

            TransactionBuilder builder = wrapper.triggerCall(cmd.fromAddress(), cmd.contractAddress(), function);
            builder.setFeeLimit(properties.getFeeLimit());
            Chain.Transaction signed = wrapper.signTransaction(builder.build());
            String txId = wrapper.broadcastTransaction(signed);
            if (!StringUtils.hasText(txId)) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{"empty txid"});
            }
            return txId;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("TRON 出款失败 from={} to={}", cmd.fromAddress(), cmd.toAddress(), e);
            throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{e.getMessage()});
        } finally {
            if (wrapper != null) {
                wrapper.close();
            }
        }
    }

    @Override
    public long confirmations(String txHash) throws BizException {
        try {
            long txBlock = client.getTxBlockNumber(txHash);
            if (txBlock <= 0) {
                return 0;
            }
            long now = client.getNowBlockNumber();
            return Math.max(0, now - txBlock + 1);
        } catch (Exception e) {
            log.error("TRON 确认数查询失败 tx={}", txHash, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public BigInteger tokenBalanceRaw(String address, String contractAddress) throws BizException {
        ApiWrapper wrapper = null;
        try {
            wrapper = newReadonlyWrapper();
            // ABI: balanceOf(address)->uint256；triggerConstantContract 为只读常量调用，不签名/不广播/不耗资源
            Function function = new Function("balanceOf",
                    List.of(new Address(address)),
                    Collections.emptyList());
            Response.TransactionExtention txn = wrapper.triggerConstantContract(address, contractAddress, function);
            List<ByteString> results = txn.getConstantResultList();
            if (results.isEmpty()) {
                return BigInteger.ZERO;
            }
            // 常量返回为 32 字节大端 uint256；按无符号整数解码
            return new BigInteger(1, results.get(0).toByteArray());
        } catch (Exception e) {
            log.error("TRON 代币余额查询失败 addr={}", address, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        } finally {
            if (wrapper != null) {
                wrapper.close();
            }
        }
    }

    @Override
    public BigInteger nativeBalanceRaw(String address) throws BizException {
        ApiWrapper wrapper = null;
        try {
            wrapper = newReadonlyWrapper();
            // getAccountBalance 直接返回 TRX 余额(sun)；账户未激活返回 0
            return BigInteger.valueOf(wrapper.getAccountBalance(address));
        } catch (Exception e) {
            log.error("TRON 原生币余额查询失败 addr={}", address, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        } finally {
            if (wrapper != null) {
                wrapper.close();
            }
        }
    }

    @Override
    public String transferNative(PayoutCommand cmd) throws BizException {
        long sun;
        try {
            // 人类可读 TRX 金额 → sun（恒 6 位）；标度超限直接失败，绝不静默舍入
            sun = cmd.amount().movePointRight(NATIVE_DECIMALS).toBigIntegerExact().longValueExact();
        } catch (ArithmeticException e) {
            throw new BizException(WalletRespCodeEnum.INVALID_AMOUNT, new Object[]{NATIVE_DECIMALS});
        }
        ApiWrapper wrapper = null;
        try {
            wrapper = newWrapper(cmd.privateKeyHex());
            // TransferContract：从 gas 供给地址转 TRX 到目标地址（TRC20 转账需目标地址有 TRX 抵能量/带宽）
            Response.TransactionExtention txn = wrapper.transfer(cmd.fromAddress(), cmd.toAddress(), sun);
            Chain.Transaction signed = wrapper.signTransaction(txn);
            String txId = wrapper.broadcastTransaction(signed);
            if (!StringUtils.hasText(txId)) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{"empty txid"});
            }
            return txId;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("TRON 原生币转账失败 from={} to={}", cmd.fromAddress(), cmd.toAddress(), e);
            throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{e.getMessage()});
        } finally {
            if (wrapper != null) {
                wrapper.close();
            }
        }
    }

    @Override
    public int nativeDecimals() {
        return NATIVE_DECIMALS;
    }

    /** 只读调用(余额/常量合约)也需 ApiWrapper 实例；用临时随机密钥占位，绝不签名广播，私钥无业务意义。 */
    private ApiWrapper newReadonlyWrapper() {
        return newWrapper(KeyPair.generate().toPrivateKey());
    }

    /** 按出款私钥构造 ApiWrapper：优先自定义 gRPC 节点，否则用 trident 内置主网（可带 TronGrid api-key）。 */
    private ApiWrapper newWrapper(String privateKeyHex) {
        if (StringUtils.hasText(properties.getGrpcEndpoint())) {
            String solidity = StringUtils.hasText(properties.getGrpcEndpointSolidity())
                    ? properties.getGrpcEndpointSolidity()
                    : properties.getGrpcEndpoint();
            return new ApiWrapper(properties.getGrpcEndpoint(), solidity, privateKeyHex);
        }
        return StringUtils.hasText(properties.getApiKey())
                ? ApiWrapper.ofMainnet(privateKeyHex, properties.getApiKey())
                : ApiWrapper.ofMainnet(privateKeyHex);
    }
}