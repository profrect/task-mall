package com.mall.mission.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 移动端任务卡片。 */
@Data
public class MissionTaskVO {

    private Long id;

    private Long taskId;

    private Long recordId;

    private String taskCode;

    private String title;

    private String description;

    private String taskType;

    private String currency;

    private BigDecimal rewardAmount;

    private Integer requiredVipLevel;

    private String userStatus;

    private String submitContent;

    private String reviewRemark;

    private Long claimedAt;

    private Long submittedAt;

    private Long reviewedAt;
}