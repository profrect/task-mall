package com.mall.wallet.chain.evm;

import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapter;
import com.mall.wallet.chain.adapter.DepositEvent;
import com.mall.wallet.chain.adapter.KeyMaterial;
import com.mall.wallet.chain.adapter.PayoutCommand;
import com.mall.wallet.enums.WalletRespCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * EVM 链适配器（ETH / BSC / Polygon 共用一份逻辑，按 {@link ChainCode} 参数化为多个 bean 实例）。
 * <p>
 * 设计：本类<strong>无链特异代码</strong>，链差异（rpcUrl/chainId/gas/lookback）全部来自注入的 {@link EvmProperties}。
 * 注册表按 chainCode 装配，新增一条 EVM 链 = 加配置 + 注册一个 bean，零新代码。
 * <p>
 * 能力边界（与 TronAdapter 完全对称）：
 * <ul>
 *   <li><strong>充值收款</strong>：{@link #newDepositAddress} 即时生成 SECP256K1 密钥对（Option D），
 *       私钥经 KeyMaterial 带出由地址分配层加密落库；地址统一小写存储。</li>
 *   <li><strong>充值扫块</strong>：{@link #scanDeposits} 用 {@code eth_getLogs} 按 to 地址(topic2)过滤拉取
 *       ERC-20 Transfer 转入，<strong>不预过滤合约</strong>——是不是 USDT、精度几位由编排层按 coin_config 判定。
 *       采用 lookback 窗口重复观测，确认数演进与幂等去重(chain,txHash,logIndex)均在编排层处理。</li>
 *   <li><strong>出款</strong>：{@link #transferOut} 用热钱包私钥签名 ERC-20 transfer 并广播；
 *       {@link #confirmations} 按 (当前块高 - 交易块高 + 1) 计算确认数。服务提现出账与未来归集。</li>
 * </ul>
 * 安全边界：私钥仅在方法栈内存活（生成后即交加密层、出款由 {@link PayoutCommand} 即时解密传入），用后即弃，不持久化、不写日志。
 */
@Slf4j
public class EvmAdapter implements ChainAdapter {

    /** ERC-20 {@code Transfer(address,address,uint256)} 事件签名 keccak256；所有 EVM 链通用常量。 */
    private static final String TRANSFER_TOPIC =
            "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    /** 单次 eth_getLogs 的 to 地址过滤批量上限：地址过多时分批，规避节点对 topic 数组长度的限制。 */
    private static final int ADDRESS_BATCH_SIZE = 100;

    /** 原生币(ETH/BNB/MATIC)精度恒为 18 位(wei)；归集喂 gas 时按此把人类可读金额归一化为 wei。 */
    private static final int NATIVE_DECIMALS = 18;

    /** 纯原生币转账固定 gas 上限 21000（无 data 的标准转账消耗）。 */
    private static final BigInteger NATIVE_TRANSFER_GAS = BigInteger.valueOf(21_000L);

    private final ChainCode chainCode;
    private final EvmProperties properties;
    /** 懒初始化的 Web3j 客户端：仅在首次真正发起链上调用时按本链 rpcUrl 建立连接。 */
    private volatile Web3j web3j;

    public EvmAdapter(ChainCode chainCode, EvmProperties properties) {
        this.chainCode = chainCode;
        this.properties = properties;
    }

    @Override
    public ChainCode chainCode() {
        return chainCode;
    }

    @Override
    public KeyMaterial newDepositAddress() throws BizException {
        try {
            ECKeyPair kp = Keys.createEcKeyPair();
            // Keys.getAddress 返回小写 40-hex(无 0x)；统一加 0x 前缀小写存储，与扫块解析出的地址大小写一致
            String address = "0x" + Keys.getAddress(kp);
            // 私钥左补零到 64-hex(无 0x)：BigInteger 去前导零可能不足 32 字节，补齐避免签名/解密歧义。落库前必经 SecretCipher 加密
            String privateKeyHex = Numeric.toHexStringNoPrefixZeroPadded(kp.getPrivateKey(), 64);
            return new KeyMaterial(address, privateKeyHex);
        } catch (Exception e) {
            log.error("EVM 地址生成失败 chain={}", chainCode, e);
            throw new BizException(WalletRespCodeEnum.ADDRESS_DERIVE_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public List<DepositEvent> scanDeposits(Collection<String> watchAddresses) throws BizException {
        if (watchAddresses == null || watchAddresses.isEmpty()) {
            return List.of();
        }
        EvmProperties.EvmChain cfg = requireConfig();
        try {
            Web3j w = web3j(cfg);
            BigInteger currentBlock = w.ethBlockNumber().send().getBlockNumber();
            // lookback 窗口须 >= 确认窗口 + 扫描间隔：一笔转入要在窗口内被持续重复观测，直到确认数达标才入账；
            // 否则它会在确认达标前滑出窗口，confirmations 停滞、永不入账。窗口大小按链确认数/出块速度配置。
            BigInteger fromBlock = currentBlock.subtract(BigInteger.valueOf(cfg.getScanLookbackBlocks()));
            if (fromBlock.signum() < 0) {
                fromBlock = BigInteger.ZERO;
            }
            DefaultBlockParameter from = DefaultBlockParameter.valueOf(fromBlock);
            DefaultBlockParameter to = DefaultBlockParameter.valueOf(currentBlock);

            List<String> addresses = new ArrayList<>(watchAddresses);
            List<DepositEvent> events = new ArrayList<>();
            // 不限合约(address 留空) + 按 to 地址(topic2)过滤：返回"任意 ERC-20 转入本平台地址"，与 TronAdapter 对称。
            // 地址过多时分批，规避节点 topic 数组长度限制；重复观测由编排层三层幂等键兜底，绝不重复入账。
            for (int i = 0; i < addresses.size(); i += ADDRESS_BATCH_SIZE) {
                List<String> batch = addresses.subList(i, Math.min(i + ADDRESS_BATCH_SIZE, addresses.size()));
                EthFilter filter = new EthFilter(from, to, Collections.<String>emptyList());
                filter.addSingleTopic(TRANSFER_TOPIC);  // topic0 = Transfer 事件签名
                filter.addNullTopic();                  // topic1 = 任意 from
                filter.addOptionalTopics(batch.stream()
                        .map(this::toAddressTopic).toArray(String[]::new)); // topic2 = 本批地址 OR 匹配

                List<EthLog.LogResult> logs = w.ethGetLogs(filter).send().getLogs();
                for (EthLog.LogResult lr : logs) {
                    DepositEvent ev = toDepositEvent(lr, currentBlock);
                    if (ev != null) {
                        events.add(ev);
                    }
                }
            }
            return events;
        } catch (Exception e) {
            log.error("EVM 扫块失败 chain={}", chainCode, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    /** 把一条链上日志归一化为充值观测事件；非标准 Transfer / reorg 移除 / 缺块高的日志返回 null 被丢弃。 */
    private DepositEvent toDepositEvent(EthLog.LogResult lr, BigInteger currentBlock) {
        if (!(lr.get() instanceof Log logEntry)) {
            return null;
        }
        // reorg 中被移除的日志直接丢弃；标准 Transfer 须有 [sig, from, to] 三个 topic
        if (logEntry.isRemoved()) {
            return null;
        }
        List<String> topics = logEntry.getTopics();
        if (topics == null || topics.size() < 3) {
            return null;
        }
        BigInteger blockNumber = logEntry.getBlockNumber();
        if (blockNumber == null) {
            return null;
        }
        long confirmations = Math.max(0L,
                currentBlock.subtract(blockNumber).add(BigInteger.ONE).longValue());
        return DepositEvent.builder()
                .chainCode(chainCode)
                // 合约地址原样小写带回，由编排层按 coin_config 匹配币种/精度(BSC-USDT 为 18 位)
                .contractAddress(logEntry.getAddress() == null ? null : logEntry.getAddress().toLowerCase())
                .txHash(logEntry.getTransactionHash())
                // EVM logIndex 为区块内全局日志序号，配合 txHash 唯一定位一条转账(同笔多 Transfer 各自成行)
                .logIndex(logEntry.getLogIndex() == null ? 0 : logEntry.getLogIndex().intValue())
                .fromAddress(topicToAddress(topics.get(1)))
                .toAddress(topicToAddress(topics.get(2)))
                .amountRaw(parseAmountRaw(logEntry.getData()))
                .blockHeight(blockNumber.longValue())
                .confirmations(confirmations)
                .build();
    }

    /** 地址 → 32 字节 topic（左补零到 64-hex 小写），用于 eth_getLogs 的 indexed 地址过滤。 */
    private String toAddressTopic(String address) {
        String clean = strip0x(address);
        return "0x" + "0".repeat(Math.max(0, 64 - clean.length())) + clean.toLowerCase();
    }

    /** 32 字节 indexed topic → 地址（取低 20 字节小写）。 */
    private String topicToAddress(String topic) {
        String clean = strip0x(topic);
        if (clean.length() < 40) {
            return "0x" + clean.toLowerCase();
        }
        return "0x" + clean.substring(clean.length() - 40).toLowerCase();
    }

    /** ERC-20 Transfer 的 value 在 data 段(uint256)；空 data 记 "0"（由编排层按金额 > 0 过滤）。 */
    private String parseAmountRaw(String data) {
        if (!StringUtils.hasText(data) || "0x".equalsIgnoreCase(data)) {
            return "0";
        }
        return Numeric.toBigInt(data).toString();
    }

    private String strip0x(String s) {
        return (s.startsWith("0x") || s.startsWith("0X")) ? s.substring(2) : s;
    }

    @Override
    public String transferOut(PayoutCommand cmd) throws BizException {
        if (!StringUtils.hasText(cmd.contractAddress())) {
            throw new BizException(WalletRespCodeEnum.COIN_CONFIG_MISSING,
                    new Object[]{chainCode.name() + " USDT contract"});
        }
        BigInteger rawAmount;
        try {
            // 归一化为链上整数（uint256）；标度超出 decimals 直接失败，绝不静默舍入
            rawAmount = cmd.amount().movePointRight(cmd.decimals()).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new BizException(WalletRespCodeEnum.INVALID_AMOUNT, new Object[]{cmd.decimals()});
        }

        EvmProperties.EvmChain cfg = requireConfig();
        try {
            Web3j w = web3j(cfg);
            Credentials credentials = Credentials.create(cmd.privateKeyHex());

            // ABI: transfer(address,uint256) —— uint256 用 BigInteger 精确承载链上整数金额
            List<Type> params = List.of(new Address(cmd.toAddress()), new Uint256(rawAmount));
            Function function = new Function("transfer", params, Collections.emptyList());
            String data = FunctionEncoder.encode(function);

            // 调代币合约、不转原生币（value=0）；EIP-155 用 chainId 签名防跨链重放
            BigInteger nonce = w.ethGetTransactionCount(cmd.fromAddress(), DefaultBlockParameterName.PENDING)
                    .send().getTransactionCount();
            BigInteger gasPrice = cfg.getGasPriceWei() != null
                    ? cfg.getGasPriceWei()
                    : w.ethGasPrice().send().getGasPrice();
            RawTransaction rawTx = RawTransaction.createTransaction(
                    nonce, gasPrice, cfg.getGasLimit(), cmd.contractAddress(), data);
            byte[] signed = TransactionEncoder.signMessage(rawTx, cfg.getChainId(), credentials);

            EthSendTransaction resp = w.ethSendRawTransaction(Numeric.toHexString(signed)).send();
            if (resp.hasError()) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR,
                        new Object[]{resp.getError().getMessage()});
            }
            String txHash = resp.getTransactionHash();
            if (!StringUtils.hasText(txHash)) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{"empty txHash"});
            }
            return txHash;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("EVM 出款失败 chain={} from={} to={}", chainCode, cmd.fromAddress(), cmd.toAddress(), e);
            throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public long confirmations(String txHash) throws BizException {
        EvmProperties.EvmChain cfg = requireConfig();
        try {
            Web3j w = web3j(cfg);
            Optional<TransactionReceipt> receipt =
                    w.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            // 回执缺失 = 尚未上链 / 不存在；回执存在即已被打包，确认数 = 当前块高 - 交易块高 + 1
            if (receipt.isEmpty() || receipt.get().getBlockNumber() == null) {
                return 0;
            }
            BigInteger txBlock = receipt.get().getBlockNumber();
            BigInteger now = w.ethBlockNumber().send().getBlockNumber();
            return Math.max(0L, now.subtract(txBlock).add(BigInteger.ONE).longValue());
        } catch (Exception e) {
            log.error("EVM 确认数查询失败 chain={} tx={}", chainCode, txHash, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public BigInteger tokenBalanceRaw(String address, String contractAddress) throws BizException {
        EvmProperties.EvmChain cfg = requireConfig();
        try {
            Web3j w = web3j(cfg);
            // ABI: balanceOf(address)->uint256；eth_call 只读，不签名/不广播/不耗 gas
            Function fn = new Function("balanceOf",
                    List.of(new Address(address)),
                    List.of(new TypeReference<Uint256>() {
                    }));
            String data = FunctionEncoder.encode(fn);
            EthCall resp = w.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction
                            .createEthCallTransaction(address, contractAddress, data),
                    DefaultBlockParameterName.LATEST).send();
            if (resp.isReverted()) {
                return BigInteger.ZERO;
            }
            List<Type> out = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            return out.isEmpty() ? BigInteger.ZERO : (BigInteger) out.get(0).getValue();
        } catch (Exception e) {
            log.error("EVM 代币余额查询失败 chain={} addr={}", chainCode, address, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public BigInteger nativeBalanceRaw(String address) throws BizException {
        EvmProperties.EvmChain cfg = requireConfig();
        try {
            return web3j(cfg).ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (Exception e) {
            log.error("EVM 原生币余额查询失败 chain={} addr={}", chainCode, address, e);
            throw new BizException(WalletRespCodeEnum.CHAIN_SCAN_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public String transferNative(PayoutCommand cmd) throws BizException {
        BigInteger value;
        try {
            // 人类可读原生币金额 → wei（恒 18 位）；标度超限直接失败，绝不静默舍入
            value = cmd.amount().movePointRight(NATIVE_DECIMALS).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new BizException(WalletRespCodeEnum.INVALID_AMOUNT, new Object[]{NATIVE_DECIMALS});
        }
        EvmProperties.EvmChain cfg = requireConfig();
        try {
            Web3j w = web3j(cfg);
            Credentials credentials = Credentials.create(cmd.privateKeyHex());
            BigInteger nonce = w.ethGetTransactionCount(cmd.fromAddress(), DefaultBlockParameterName.PENDING)
                    .send().getTransactionCount();
            BigInteger gasPrice = cfg.getGasPriceWei() != null
                    ? cfg.getGasPriceWei()
                    : w.ethGasPrice().send().getGasPrice();
            // 纯原生币转账：to=收款地址、value=金额、无 data、gasLimit 固定 21000；EIP-155 用 chainId 防重放
            RawTransaction rawTx = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, NATIVE_TRANSFER_GAS, cmd.toAddress(), value);
            byte[] signed = TransactionEncoder.signMessage(rawTx, cfg.getChainId(), credentials);
            EthSendTransaction sendResp = w.ethSendRawTransaction(Numeric.toHexString(signed)).send();
            if (sendResp.hasError()) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR,
                        new Object[]{sendResp.getError().getMessage()});
            }
            String txHash = sendResp.getTransactionHash();
            if (!StringUtils.hasText(txHash)) {
                throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{"empty txHash"});
            }
            return txHash;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("EVM 原生币转账失败 chain={} from={} to={}", chainCode, cmd.fromAddress(), cmd.toAddress(), e);
            throw new BizException(WalletRespCodeEnum.WITHDRAW_BROADCAST_ERROR, new Object[]{e.getMessage()});
        }
    }

    @Override
    public int nativeDecimals() {
        return NATIVE_DECIMALS;
    }

    /** 取本链配置，未配置 rpcUrl 即视为「该链未接入」，明确报错而非 NPE。 */
    private EvmProperties.EvmChain requireConfig() throws BizException {
        EvmProperties.EvmChain cfg = properties.chain(chainCode.name());
        if (cfg == null || !StringUtils.hasText(cfg.getRpcUrl())) {
            throw new BizException(WalletRespCodeEnum.CHAIN_NOT_SUPPORTED,
                    new Object[]{chainCode.name() + " 未配置 rpcUrl"});
        }
        return cfg;
    }

    /** 双检锁懒建 Web3j：同一适配器只维护一个连接，按本链 rpcUrl 建立。 */
    private Web3j web3j(EvmProperties.EvmChain cfg) {
        Web3j w = this.web3j;
        if (w == null) {
            synchronized (this) {
                w = this.web3j;
                if (w == null) {
                    w = Web3j.build(new HttpService(cfg.getRpcUrl()));
                    this.web3j = w;
                }
            }
        }
        return w;
    }
}