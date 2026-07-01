package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 邀请返佣记录响应：连接邀请关系、触发业务和钱包入账流水。 */
@Data
public class InviteCommissionRecordResp {

    private Long id;

    private String recordNo;

    private Long inviterUserId;

    private Long sourceUserId;

    private String sourceOrderNo;

    private String businessType;

    private String currency;

    private BigDecimal sourceAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long settledAt;

    private Long createTime;

    private Long updateTime;
}