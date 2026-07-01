package com.mall.wallet.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 钱包敏感数据加密配置。
 * <p>
 * masterKey：AES 主密钥，base64 编码的 16/24/32 字节。<strong>走环境变量注入，不入库不进 git。</strong>
 * 阶段2会被 KMS 托管替换（届时本配置可下线）。
 */
@Data
@ConfigurationProperties(prefix = "wallet.security")
public class WalletSecurityProperties {

    /** base64(AES key)，16/24/32 字节分别对应 AES-128/192/256。 */
    private String masterKey;
}