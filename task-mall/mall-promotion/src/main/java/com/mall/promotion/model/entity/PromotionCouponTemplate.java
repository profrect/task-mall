package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 优惠券模板事实：只定义券规则和库存，不承载用户领取状态。 */
@Data
@Table(value = "promotion_coupon_template", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionCouponTemplate extends BaseEntity<Long> {

    private String couponCode;

    private String title;

    private String description;

    private String couponType;

    private String currency;

    private BigDecimal discountAmount;

    private BigDecimal minOrderAmount;

    private Integer totalStock;

    private Integer claimedStock;

    private Integer perUserLimit;

    private Integer validDays;

    private Long startAt;

    private Long endAt;

    private Integer sortOrder;

    private Integer status;
}