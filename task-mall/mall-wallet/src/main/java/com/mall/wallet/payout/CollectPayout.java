package com.mall.wallet.payout;

import java.math.BigDecimal;

/**
 * 归集结果（签名边界 → 编排层的边界 DTO）。
 * <p>
 * 与 {@link PayoutResult} 的差异：归集额在<strong>广播时刻</strong>才由「实时全部余额」确定，
 * 故须带回 {@code sweptAmount} 供订单回填（区别于提现的固定申请额）。
 *
 * @param toAddress   归集目标地址（本链出款热钱包，回填订单 to_address）
 * @param sweptAmount 实际归集转出额（人类可读单位 = 广播时刻地址全部 USDT 余额）
 * @param txHash      归集交易哈希
 */
public record CollectPayout(String toAddress, BigDecimal sweptAmount, String txHash) {
}