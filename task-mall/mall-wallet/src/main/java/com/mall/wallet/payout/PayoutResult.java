package com.mall.wallet.payout;

/**
 * 出款结果（签名边界 → 编排层的边界 DTO）。
 *
 * @param fromAddress 实际出款热钱包地址（回填订单，便于对账 / 链上追踪）
 * @param txHash      链上交易哈希
 */
public record PayoutResult(String fromAddress, String txHash) {
}