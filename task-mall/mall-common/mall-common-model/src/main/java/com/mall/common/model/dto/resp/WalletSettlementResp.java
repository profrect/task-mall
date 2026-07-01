package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 钱包结算响应：返回实际命中的账务流水，重复请求会返回同一笔幂等流水。 */
@Data
public class WalletSettlementResp {

    private String flowNo;

    private Long userId;

    private String currency;

    private String bizType;

    private String bizId;

    private String direction;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String remark;

    private Long createTime;
}