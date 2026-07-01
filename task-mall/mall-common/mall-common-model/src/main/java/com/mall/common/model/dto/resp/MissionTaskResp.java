package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 任务配置响应。 */
@Data
public class MissionTaskResp {

    private Long id;

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

    private Long createTime;

    private Long updateTime;
}