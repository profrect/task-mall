package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 用户抽奖记录：记录活动、奖品快照与钱包入账流水，作为抽奖对账主事实。 */
@Data
@Table(value = "promotion_lottery_record", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionLotteryRecord extends BaseEntity<Long> {

    private String recordNo;

    private Long userId;

    private Long activityId;

    private String activityTitle;

    private Long prizeId;

    private String prizeCode;

    private String prizeName;

    private String prizeType;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long drawnAt;

    private Long settledAt;
}