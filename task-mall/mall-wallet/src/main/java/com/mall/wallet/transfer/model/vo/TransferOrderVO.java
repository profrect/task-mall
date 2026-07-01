package com.mall.wallet.transfer.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

/** 站内转账订单视图。 */
@Builder
public record TransferOrderVO(
        String orderNo,
        Long fromUserId,
        Long toUserId,
        String coin,
        BigDecimal amount,
        String status,
        String remark,
        Long finishedAt,
        Long createTime
) {
}