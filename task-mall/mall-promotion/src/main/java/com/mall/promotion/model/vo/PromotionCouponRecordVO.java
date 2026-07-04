package com.mall.promotion.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionCouponRecordVO {

    private Long id;

    private String recordNo;

    private Long templateId;

    private String couponCode;

    private String title;

    private String couponType;

    private String currency;

    private BigDecimal discountAmount;

    private BigDecimal minOrderAmount;

    private String status;

    private Long validFrom;

    private Long validTo;

    private Long claimedAt;

    private Long lockedAt;

    private Long usedAt;

    private Long expiredAt;
}