package com.mall.common.model.dto.req;

import lombok.Data;

/** 抽奖活动配置请求。 */
@Data
public class PromotionLotteryActivityReq {

    private Long id;

    private String activityCode;

    private String title;

    private String description;

    private Integer dailyLimit;

    private Long startAt;

    private Long endAt;

    private Integer sortOrder;

    private Integer status;
}