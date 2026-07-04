package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 用户每日签到事实：唯一约束为 userId + checkinDate，奖励通过钱包结算入账。 */
@Data
@Table(value = "promotion_checkin_record", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionCheckinRecord extends BaseEntity<Long> {

    private String recordNo;

    private Long userId;

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