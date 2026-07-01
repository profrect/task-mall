package com.mall.wallet.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 离线私钥加密 CLI：把明文私钥加密为 {@code wallet.hotwallet.chains.*.private-key-cipher} 与
 * {@code wallet.gas-station.chains.*.private-key-cipher} 配置项所需的密文。
 * <p>
 * 为何需要：{@link SecretCipher} 是 Spring 容器内组件，运维无法独立调用；而热钱包出款 / 归集 gas 代付
 * 上线前，必须先把明文私钥加密成密文填入配置。本工具不依赖 Spring 容器，<strong>复用同一个
 * {@link SecretCipher} 的 AES-256-GCM 算法与主密钥校验</strong>，杜绝另起一套加密实现导致的密文格式漂移。
 * <p>
 * 安全约束：
 * <ul>
 *   <li>私钥仅从 stdin 读取（不走命令行参数），避免明文落入 shell history / ps / 进程参数表；</li>
 *   <li>主密钥从环境变量读取，须与运行服务的 {@code wallet.security.master-key} 完全一致；</li>
 *   <li>全程不写文件、不联网、不回显明文；加密后立即解回自检，确认服务端可正确解密再输出。</li>
 * </ul>
 * <pre>
 * 用法（Spring Boot fat jar，3.2+ 用 PropertiesLauncher）：
 *   echo -n '&lt;明文私钥&gt;' | WALLET_SECURITY_MASTER_KEY='&lt;base64主密钥&gt;' \
 *     java -Dloader.main=com.mall.wallet.security.PrivateKeyCipherCli \
 *       -cp mall-wallet.jar org.springframework.boot.loader.launch.PropertiesLauncher
 *
 * 输出：单行密文到 stdout，复制到对应链的 *_PK_CIPHER 环境变量（如 WALLET_HOTWALLET_TRON_PK_CIPHER）。
 * </pre>
 */
public final class PrivateKeyCipherCli {

    private PrivateKeyCipherCli() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            printUsage();
            return;
        }

        String masterKey = System.getenv("WALLET_SECURITY_MASTER_KEY");
        if (masterKey == null || masterKey.isBlank()) {
            System.err.println("缺少环境变量 WALLET_SECURITY_MASTER_KEY（base64 AES 主密钥，须与运行服务一致）");
            printUsage();
            System.exit(2);
            return;
        }

        // 手工装配 properties 后复用 SecretCipher：沿用其算法与主密钥 fail-fast 校验，不重复实现加密
        WalletSecurityProperties properties = new WalletSecurityProperties();
        properties.setMasterKey(masterKey);
        SecretCipher cipher = new SecretCipher(properties);

        String plaintext = readSecretFromStdin();
        if (plaintext.isEmpty()) {
            System.err.println("未从 stdin 读到私钥明文");
            printUsage();
            System.exit(2);
            return;
        }

        String cipherText = cipher.encrypt(plaintext);
        // 自检：立即解回比对，确保密文可被服务端正确解密，避免输出一段无法使用的坏密文
        if (!plaintext.equals(cipher.decrypt(cipherText))) {
            System.err.println("内部自检失败：密文无法解回原文，请勿使用");
            System.exit(1);
            return;
        }
        System.out.println(cipherText);
    }

    /** 从 stdin 读取私钥明文（而非命令行参数），避免明文落入 shell history / ps / 进程参数表。 */
    private static String readSecretFromStdin() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            return sb.toString().strip();
        }
    }

    private static void printUsage() {
        System.err.println("""
                离线私钥加密工具 —— 生成 *.private-key-cipher 配置值
                用法（私钥从 stdin 读取，不落入 shell 历史 / 进程参数）：
                  echo -n '<明文私钥>' | WALLET_SECURITY_MASTER_KEY='<base64主密钥>' \\
                    java -Dloader.main=com.mall.wallet.security.PrivateKeyCipherCli \\
                      -cp mall-wallet.jar org.springframework.boot.loader.launch.PropertiesLauncher
                说明：
                  - WALLET_SECURITY_MASTER_KEY 必须与服务的 wallet.security.master-key 完全一致，否则服务端解不开。
                  - 密文单行输出到 stdout，复制到对应链的 *_PK_CIPHER 环境变量。
                  - 本工具不写文件、不联网、不回显明文私钥。""");
    }
}