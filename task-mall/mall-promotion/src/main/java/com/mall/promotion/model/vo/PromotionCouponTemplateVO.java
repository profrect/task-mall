package com.mall.promotion.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionCouponTemplateVO {

    private Long id;

    private String couponCode;

    private String title;

    private String description;

    private String couponType;

    private String currency;

    private BigDecimal discountAmount;

    private BigDecimal minOrderAmount;

    private Integer totalStock;

    private Integer claimedStock;

    private Integer remainStock;

    private Integer perUserLimit;

    private Integer userClaimedCount;

    private Integer validDays;

    private Long startAt;

    private Long endAt;

    private Integer status;
}