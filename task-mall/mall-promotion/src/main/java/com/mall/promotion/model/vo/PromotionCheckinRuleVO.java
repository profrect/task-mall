package com.mall.promotion.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionCheckinRuleVO {

    private Long id;

    private String ruleCode;

    private String title;

    private Integer requiredConsecutiveDays;

    private String currency;

    private BigDecimal rewardAmount;

    private Integer status;
}