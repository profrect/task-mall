package com.mall.wallet.chain.adapter;

import com.mall.wallet.chain.ChainCode;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 出款指令（签名边界 → 适配层的边界 DTO）。
 * <p>
 * 同一指令同时服务「提现出款」（from = 热钱包）与未来「归集」（from = 用户充值地址）：
 * 二者本质都是"用某地址的私钥，把一定数量的代币转给目标地址并广播"。
 * <p>
 * 安全边界：privateKeyHex 由上层（PayoutService）即时解密得到，仅在内存流转；
 * 适配器用后即弃，严禁持久化或写日志。阶段2迁移 KMS 后，本字段改为"签名器引用"，适配层零改动。
 *
 * @param chainCode       目标链
 * @param fromAddress     出款地址（与 privateKeyHex 对应）
 * @param privateKeyHex   出款地址私钥（hex，明文仅在内存）
 * @param toAddress       收款地址
 * @param amount          转出金额（人类可读单位，如 12.5 USDT；适配器按 decimals 归一化为链上整数）
 * @param contractAddress 代币合约地址（原生币留空）
 * @param decimals        代币链上精度（小数位）
 */
@Builder
public record PayoutCommand(
        ChainCode chainCode,
        String fromAddress,
        String privateKeyHex,
        String toAddress,
        BigDecimal amount,
        String contractAddress,
        int decimals
) {
}