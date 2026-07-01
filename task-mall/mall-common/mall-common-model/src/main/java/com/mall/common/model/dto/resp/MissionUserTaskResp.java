package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 用户任务记录响应，审核通过时可对账到钱包流水。 */
@Data
public class MissionUserTaskResp {

    private Long id;

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

    private Long createTime;

    private Long updateTime;
}