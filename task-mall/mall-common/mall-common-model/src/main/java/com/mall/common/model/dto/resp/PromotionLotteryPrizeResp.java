package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 活动奖池项响应，带奖品快照字段，便于后台配置和移动端展示。 */
@Data
public class PromotionLotteryPrizeResp {

    private Long id;

    private Long activityId;

    private Long prizeId;

    private String prizeCode;

    private String prizeName;

    private String prizeType;

    private String currency;

    private BigDecimal amount;

    private Integer weight;

    private Integer dailyLimit;

    private Integer sortOrder;

    private Integer status;

    private Long createTime;

    private Long updateTime;
}