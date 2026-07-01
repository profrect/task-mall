package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 支付通道审计订单视图，只表达外部通道/支付观察事实，不承载钱包入账动作。 */
@Data
public class PaymentOrderResp {

    private Long id;

    private String orderNo;

    private Long userId;

    private String businessType;

    private String businessOrderNo;

    private String channelCode;

    private String channelOrderNo;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String payAddress;

    private String payerAddress;

    private String txHash;

    private String auditRemark;

    private Long paidAt;

    private Long expiredAt;

    private Long createTime;

    private Long updateTime;
}