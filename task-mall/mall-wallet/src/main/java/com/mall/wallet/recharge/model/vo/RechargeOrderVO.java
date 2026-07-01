package com.mall.wallet.recharge.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * 充值记录视图（用户侧）。只暴露用户需要看到的字段，隐藏内部审计列。
 */
@Builder
public record RechargeOrderVO(
        String orderNo,
        String chainCode,
        String coin,
        BigDecimal amount,
        String fromAddress,
        String toAddress,
        String txHash,
        Integer confirmations,
        String status,
        Long creditedAt,
        Long createTime
) {
}