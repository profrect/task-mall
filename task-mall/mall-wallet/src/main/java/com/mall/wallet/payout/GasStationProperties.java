package com.mall.wallet.payout;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * gas 供给钱包配置（prefix=wallet.gas-station）。
 * <p>
 * 归集的对象是用户充值地址：它们是随机生成的密钥对，链上通常没有原生币，无法自付代币转账手续费
 * （EVM 的 gas / TRON 的能量带宽折算 TRX）。因此归集前需由一个独立的 gas 供给钱包先转一点原生币过去，
 * 这笔小额原生币即「gas 代付」。每条链一个 gas 供给钱包：地址 + 私钥密文 + 每次代付额 + 触发阈值。
 * <p>
 * 安全约定：与 {@link HotWalletProperties} 同构——私钥仅以 SecretCipher(AES-GCM) 密文经环境变量注入，
 * 绝不明文进 git/库；运行期即时解密、用后即弃。gas 钱包与出款热钱包\u003cstrong\u003e刻意分离\u003c/strong\u003e：
 * 出款热钱包是归集的\u003cstrong\u003e目标\u003c/strong\u003e（聚集资金），gas 钱包是\u003cstrong\u003e燃料源\u003c/strong\u003e（持续小额外付），职责与风险面不同。
 */
@Data
@ConfigurationProperties(prefix = "wallet.gas-station")
public class GasStationProperties {

    /** 每链 gas 供给钱包，key = ChainCode 名（TRON/ETH/BSC/POLYGON）。 */
    private Map<String, ChainGasStation> chains = new HashMap<>();

    /** 取某链 gas 供给钱包，未配置返回 null（调用方据此判定「该链未配置 gas 代付」）。 */
    public ChainGasStation station(String chainCode) {
        return chains.get(chainCode);
    }

    @Data
    public static class ChainGasStation {

        /** gas 供给地址（链上代付原生币的源地址）。 */
        private String address;

        /** gas 供给私钥密文：base64(iv‖ciphertext+tag)，由 SecretCipher 加密；明文私钥仅运行期内存可见。 */
        private String privateKeyCipher;

        /** 每次代付的原生币数量（人类可读单位，如 ETH 0.002 / BNB 0.0005 / MATIC 0.05 / TRX 30）。 */
        private BigDecimal fundAmount;

        /** 触发代付的阈值：用户地址原生币低于此值才代付，避免对已有 gas 的地址重复喂（人类可读单位）。 */
        private BigDecimal minNative;
    }
}