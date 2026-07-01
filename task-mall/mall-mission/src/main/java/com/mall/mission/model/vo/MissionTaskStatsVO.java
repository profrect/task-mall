package com.mall.mission.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 用户任务统计，只聚合真实 mission_user_task 与奖励结算事实。 */
@Data
@Builder
public class MissionTaskStatsVO {

    private Long completedCount;

    private Long inProgressCount;

    private BigDecimal totalEarnings;
}