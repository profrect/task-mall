package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 排行榜行：聚合事实 + 用户展示资料。 */
@Data
public class LeaderboardItemResp {

    private Integer rankNo;

    private Long userId;

    private String displayName;

    private Integer vipLevel;

    private String type;

    private String metricLabel;

    private BigDecimal metricValue;

    private String currency;

    private Long recordCount;

    private Long lastEventTime;
}