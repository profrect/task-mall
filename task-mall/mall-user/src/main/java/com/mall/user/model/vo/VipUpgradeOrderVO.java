package com.mall.user.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

/** VIP 升级订单响应：成功订单可通过 orderNo 对账钱包流水。 */
@Builder
public record VipUpgradeOrderVO(
        String orderNo,
        Integer fromLevel,
        Integer toLevel,
        String currency,
        BigDecimal amount,
        String status,
        String walletFlowNo,
        Long finishedAt
) {}