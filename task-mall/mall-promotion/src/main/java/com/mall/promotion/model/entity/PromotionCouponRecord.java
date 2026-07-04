package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 用户优惠券事实：领取、锁定、使用、过期都只改变本表状态。 */
@Data
@Table(value = "promotion_coupon_record", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionCouponRecord extends BaseEntity<Long> {

    private String recordNo;

    private Long userId;

    private Long templateId;

    private String couponCode;

    private String title;

    private String couponType;

    private String currency;

    private BigDecimal discountAmount;

    private BigDecimal minOrderAmount;

    private String status;

    private String lockedBizType;

    private String lockedBizId;

    private String usedBizType;

    private String usedBizId;

    private Long validFrom;

    private Long validTo;

    private Long claimedAt;

    private Long lockedAt;

    private Long usedAt;

    private Long expiredAt;
}