package com.mall.wallet.payout;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 热钱包出款配置（prefix=wallet.hotwallet）。
 * <p>
 * 每条链一个出款热钱包：地址 + 私钥密文。私钥<strong>密文</strong>由 SecretCipher(AES-GCM) 用同一 master-key
 * 离线加密产生，经环境变量注入，绝不以明文进 git/库。运行期由 {@code PayoutService} 即时解密 → 适配器签名 → 用后即弃。
 * <p>
 * KMS 迁移点：阶段2把 privateKeyCipher 改为 KMS 密钥引用，PayoutService 的解密实现替换为 KMS 调用，
 * 适配层与编排层零改动。
 */
@Data
@ConfigurationProperties(prefix = "wallet.hotwallet")
public class HotWalletProperties {

    /** 每链热钱包，key = ChainCode 名（TRON/ETH/BSC/POLYGON）。 */
    private Map<String, ChainHotWallet> chains = new HashMap<>();

    @Data
    public static class ChainHotWallet {

        /** 出款热钱包地址（链上转出方）。 */
        private String address;

        /** 出款私钥密文：base64(iv‖ciphertext+tag)，由 SecretCipher 加密；明文私钥仅运行期内存可见。 */
        private String privateKeyCipher;
    }
}