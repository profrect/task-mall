package com.mall.wallet.chain.evm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * EVM 多链接入配置（prefix=wallet.evm）。
 * <p>
 * ETH / BSC / Polygon 共用同一套 ERC-20 交互逻辑，差异仅为数据：rpcUrl + chainId + gas 参数。
 * 因此 EvmAdapter 是「一份逻辑 + 每链一组配置」的数据驱动适配器——新增一条 EVM 链 = 加一段配置 + 注册一个 bean，
 * 无需新代码。
 * <p>
 * 安全约定：rpcUrl 走 yml/环境变量，不入库不进 git；私钥不在此处，由 hotwallet 配置以密文形式注入。
 */
@Data
@ConfigurationProperties(prefix = "wallet.evm")
public class EvmProperties {

    /** 每链 EVM 配置，key = ChainCode 名（ETH/BSC/POLYGON）。 */
    private Map<String, EvmChain> chains = new HashMap<>();

    /** 取某链配置，未配置返回 null（适配器据此判定「该链未接入」）。 */
    public EvmChain chain(String chainCode) {
        return chains.get(chainCode);
    }

    @Data
    public static class EvmChain {

        /** JSON-RPC 节点地址，如 https://eth.llamarpc.com。 */
        private String rpcUrl;

        /** EVM chainId（ETH=1, BSC=56, Polygon=137），用于 EIP-155 签名防重放。 */
        private long chainId;

        /** 出款 gas 上限（ERC-20 transfer 经验值约 6.5 万，留足余量默认 10 万）。 */
        private BigInteger gasLimit = BigInteger.valueOf(100_000L);

        /** 出款 gasPrice（wei）。留空则用节点 eth_gasPrice 实时报价（推荐）。 */
        private BigInteger gasPriceWei;

        /**
         * 充值扫块回看区块数：每轮扫 [currentBlock - N, currentBlock]。
         * <p>
         * 必须 >= 确认窗口 + 扫描间隔对应的区块数，否则转入在确认数达标前即滑出窗口、永不入账。
         * 经验值（30s 扫描间隔）：ETH(确认12/出块~12s)≈50；BSC(确认15/~3s)≈100；Polygon(确认128/~2s)≈300。
         */
        private int scanLookbackBlocks = 300;
    }
}