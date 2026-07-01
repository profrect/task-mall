package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/** 任务配置请求。任务奖励最终由审核通过后的钱包结算产生。 */
@Data
public class MissionTaskReq {

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
}