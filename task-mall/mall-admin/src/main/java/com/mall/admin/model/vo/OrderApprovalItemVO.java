package com.mall.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 后台订单审核聚合项。 */
@Data
@Builder
public class OrderApprovalItemVO {

    private String sourceType;

    private String sourceId;

    private String title;

    private Long userId;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String detail;

    private Long submittedAt;

    private Long createTime;
}