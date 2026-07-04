package com.mall.promotion.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionCheckinRecordVO {

    private Long id;

    private String recordNo;

    private Integer checkinDate;

    private Integer consecutiveDays;

    private String currency;

    private BigDecimal rewardAmount;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long checkedAt;

    private Long settledAt;
}