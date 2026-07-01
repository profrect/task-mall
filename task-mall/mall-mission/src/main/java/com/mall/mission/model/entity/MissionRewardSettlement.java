package com.mall.mission.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 任务奖励结算记录：连接任务审核事实与钱包流水。 */
@Data
@Table(value = "mission_reward_settlement", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class MissionRewardSettlement extends BaseEntity<Long> {

    private Long userTaskId;

    private Long userId;

    private Long taskId;

    private String currency;

    private BigDecimal amount;

    private String bizType;

    private String bizId;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long settledAt;
}