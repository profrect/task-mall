package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 用户抽奖记录响应：记录中奖快照和钱包入账流水号。 */
@Data
public class PromotionLotteryRecordResp {

    private Long id;

    private String recordNo;

    private Long userId;

    private Long activityId;

    private String activityTitle;

    private Long prizeId;

    private String prizeCode;

    private String prizeName;

    private String prizeType;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long drawnAt;

    private Long settledAt;

    private Long createTime;

    private Long updateTime;
}