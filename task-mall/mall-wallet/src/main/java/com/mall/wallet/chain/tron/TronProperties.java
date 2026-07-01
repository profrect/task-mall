package com.mall.wallet.chain.tron;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TRON / TronGrid 接入配置（prefix=wallet.tron）。
 * <p>
 * apiKey 走环境变量注入，不入库不进 git；endpoint 默认主网 TronGrid。
 */
@Data
@ConfigurationProperties(prefix = "wallet.tron")
public class TronProperties {

    /** TronGrid / FullNode 基础地址，如 https://api.trongrid.io。 */
    private String endpoint = "https://api.trongrid.io";

    /** TronGrid API Key（TRON-PRO-API-KEY）；为空则按免密限流访问。 */
    private String apiKey;

    /** USDT-TRC20 合约地址（主网默认）。 */
    private String usdtContract = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    /** 业务入账所需最小确认数。 */
    private int minConfirmations = 19;

    /** 单地址单次扫描拉取的最近转账条数。 */
    private int scanLimit = 50;

    /** TronGrid 调用超时（毫秒）。 */
    private int timeoutMillis = 8000;

    /** 出款 gRPC 节点（trident ApiWrapper）。留空则用 trident 内置主网 gRPC（ofMainnet）。 */
    private String grpcEndpoint;

    /** 出款 gRPC solidity 节点（自定义 grpcEndpoint 时配套，留空则复用 grpcEndpoint）。 */
    private String grpcEndpointSolidity;

    /** 提现/归集 TRC20 转账 feeLimit（sun，1 TRX = 1e6 sun）。默认 100 TRX，避免能量不足广播失败。 */
    private long feeLimit = 100_000_000L;
}