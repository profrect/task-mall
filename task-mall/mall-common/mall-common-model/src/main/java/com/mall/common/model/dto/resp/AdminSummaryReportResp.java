package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 管理端综合报表快照：由 mall-admin 聚合 user 与 wallet 的只读统计。 */
@Data
public class AdminSummaryReportResp {

    private Long totalUsers;

    private Long todayNewUsers;

    private BigDecimal totalBalance;

    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private Long walletAccounts;

    private Long rechargeOrders;

    private BigDecimal rechargeAmount;

    private Long todayRechargeOrders;

    private BigDecimal todayRechargeAmount;

    private Long withdrawOrders;

    private BigDecimal withdrawAmount;

    private Long reviewingWithdrawOrders;

    private Long todayWithdrawOrders;

    private BigDecimal todayWithdrawAmount;

    private Long collectOrders;

    private BigDecimal collectedAmount;

    private Long activeCollectOrders;

    private Long todayCollectOrders;

    private BigDecimal todayCollectedAmount;

    /** 生成快照的服务时间（UTC 毫秒）。 */
    private Long generatedAt;
}