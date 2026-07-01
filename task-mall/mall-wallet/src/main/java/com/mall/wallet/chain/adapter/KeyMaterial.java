package com.mall.wallet.chain.adapter;

/**
 * 新充值地址的密钥材料（适配层 → 地址分配层的边界 DTO）。
 * <p>
 * 语义按策略而定：
 * - Option D（阶段1 TRON）：适配器即时生成密钥对，{@code address} 为收款地址，
 *   {@code privateKeyHex} 为对应私钥（hex）。调用方<strong>必须</strong>立即用 SecretCipher 加密后落库，
 *   严禁明文持久化或写入日志。
 * - 未来「公钥侧派生 / 地址池」策略：{@code privateKeyHex} 为 {@code null}，表示业务侧不持有私钥。
 *
 * @param address       链上收款地址
 * @param privateKeyHex 私钥（hex）；为 null 表示无业务侧私钥
 */
public record KeyMaterial(String address, String privateKeyHex) {

    public boolean hasPrivateKey() {
        return privateKeyHex != null && !privateKeyHex.isBlank();
    }
}