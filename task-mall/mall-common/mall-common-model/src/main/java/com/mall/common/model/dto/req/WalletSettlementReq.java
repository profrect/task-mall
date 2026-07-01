package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/** 跨服务钱包结算请求：业务服务只声明资金事件，资金方向由 mall-wallet 的 bizType 决定。 */
@Data
public class WalletSettlementReq {

    private Long userId;

    private String currency;

    private String bizType;

    private String bizId;

    private BigDecimal amount;

    private String remark;
}