package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 活动奖池项。 */
@Data
@Table(value = "promotion_lottery_prize", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionLotteryPrize extends BaseEntity<Long> {

    private Long activityId;

    private Long prizeId;

    private Integer weight;

    private Integer dailyLimit;

    private Integer sortOrder;

    private Integer status;
}