package com.mall.wallet.withdraw.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * 提现记录视图（用户侧）。暴露金额拆解(申请额/手续费/到账额)与状态机进展；
 * 隐藏审核人身份(reviewer)与内部审计列，保留 reviewRemark 以便用户看到驳回原因。
 */
@Builder
public record WithdrawOrderVO(
        String orderNo,
        String chainCode,
        String coin,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal receiveAmount,
        String toAddress,
        String txHash,
        Integer confirmations,
        String status,
        String reviewRemark,
        Long reviewedAt,
        Long broadcastAt,
        Long finishedAt,
        Long createTime
) {
}