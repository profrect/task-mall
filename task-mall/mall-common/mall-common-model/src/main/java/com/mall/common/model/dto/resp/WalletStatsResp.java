package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 钱包资金统计快照（跨服务契约：mall-wallet provider → mall-admin 报表）。 */
@Data
public class WalletStatsResp {

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
}