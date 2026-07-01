package com.mall.wallet.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 敏感数据对称加密器（AES-256-GCM）。阶段1用于充值地址私钥的「加密入库」。
 * <p>
 * 设计要点：
 * - 单一职责：只负责 encrypt/decrypt，不关心调用方存什么；阶段2把实现换成 KMS 调用，调用方零改动。
 * - 每条记录使用独立随机 IV(12B)，密文格式 = base64( iv ‖ ciphertext+tag )，自包含、可独立解密。
 * - GCM 自带完整性校验：密文被篡改 decrypt 会抛异常，不会静默返回错误明文。
 * - 启动即校验主密钥长度，缺失/非法直接 fail-fast，避免运行期才暴雷。
 */
@Component
public class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecretCipher(WalletSecurityProperties properties) {
        if (!StringUtils.hasText(properties.getMasterKey())) {
            throw new IllegalStateException("缺少 wallet.security.master-key（base64 AES 密钥），请通过环境变量注入");
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(properties.getMasterKey().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("wallet.security.master-key 不是合法 base64", e);
        }
        if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
            throw new IllegalStateException(
                    "wallet.security.master-key 解码后必须为 16/24/32 字节，当前 " + raw.length + " 字节");
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    /** 明文 -> base64( iv ‖ ciphertext+tag )。 */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packed = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    /** base64( iv ‖ ciphertext+tag ) -> 明文。 */
    public String decrypt(String packedBase64) {
        try {
            byte[] packed = Base64.getDecoder().decode(packedBase64);
            byte[] iv = Arrays.copyOfRange(packed, 0, IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(packed, IV_LENGTH, packed.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败", e);
        }
    }
}