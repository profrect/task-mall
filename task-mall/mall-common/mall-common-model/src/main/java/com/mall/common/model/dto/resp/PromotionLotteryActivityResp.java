package com.mall.common.model.dto.resp;

import lombok.Data;

/** 抽奖活动配置响应。 */
@Data
public class PromotionLotteryActivityResp {

    private Long id;

    private String activityCode;

    private String title;

    private String description;

    private Integer dailyLimit;

    private Long startAt;

    private Long endAt;

    private Integer sortOrder;

    private Integer status;

    private Long createTime;

    private Long updateTime;
}