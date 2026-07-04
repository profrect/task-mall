package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 签到奖励规则：按连续签到天数匹配，未命中时回退到第 1 天规则。 */
@Data
@Table(value = "promotion_checkin_rule", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionCheckinRule extends BaseEntity<Long> {

    private String ruleCode;

    private String title;

    private Integer requiredConsecutiveDays;

    private String currency;

    private BigDecimal rewardAmount;

    private Integer sortOrder;

    private Integer status;
}