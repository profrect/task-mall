package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 抽奖活动配置事实。 */
@Data
@Table(value = "promotion_lottery_activity", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionLotteryActivity extends BaseEntity<Long> {

    private String activityCode;

    private String title;

    private String description;

    private Integer dailyLimit;

    private Long startAt;

    private Long endAt;

    private Integer sortOrder;

    private Integer status;
}