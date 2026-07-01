package com.mall.mission.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 用户任务记录：领取、提交、审核状态机的业务事实。 */
@Data
@Table(value = "mission_user_task", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class MissionUserTask extends BaseEntity<Long> {

    private Long userId;

    private Long taskId;

    private String taskCode;

    private String taskTitle;

    private String taskType;

    private String currency;

    private BigDecimal rewardAmount;

    private String status;

    private String submitContent;

    private String reviewRemark;

    private String walletFlowNo;

    private Long claimedAt;

    private Long submittedAt;

    private Long reviewedAt;

    private Long finishedAt;
}