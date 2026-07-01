package com.mall.common.model.dto.req;

import lombok.Data;

/** 抽奖活动奖池配置请求。 */
@Data
public class PromotionLotteryPrizeReq {

    private Long id;

    private Long activityId;

    private Long prizeId;

    /** 权重，必须大于 0；实际概率 = 本项权重 / 活动启用奖项总权重。 */
    private Integer weight;

    private Integer dailyLimit;

    private Integer sortOrder;

    private Integer status;
}