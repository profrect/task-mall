package com.mall.common.model.dto.resp;

import lombok.Data;

import java.util.List;

/** 移动端抽奖活动详情：活动配置 + 启用奖池。 */
@Data
public class PromotionLotteryDetailResp {

    private PromotionLotteryActivityResp activity;

    private List<PromotionLotteryPrizeResp> prizes;

    private Integer todayDrawCount;

    private Integer todayRemainCount;
}