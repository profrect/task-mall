package com.mall.mission.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 可领取任务配置。奖励不会在领取/提交时入账，只在审核通过后进入钱包结算。 */
@Data
@Table(value = "mission_task", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class MissionTask extends BaseEntity<Long> {

    private String taskCode;

    private String title;

    private String description;

    private String taskType;

    private String currency;

    private BigDecimal rewardAmount;

    private Integer requiredVipLevel;

    private Integer dailyLimit;

    private Long startAt;

    private Long endAt;

    private Integer sortOrder;

    private Integer status;
}