package com.mall.user.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VipLevelConfigVO(
        Integer level,
        String levelName,
        BigDecimal price,
        BigDecimal rebateRate,
        Integer dailyTasks,
        String benefits
) {}
