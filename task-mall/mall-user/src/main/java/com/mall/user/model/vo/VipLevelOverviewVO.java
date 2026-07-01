package com.mall.user.model.vo;

import lombok.Builder;

import java.util.List;

/** VIP 页面聚合：当前等级 + 已启用等级配置。 */
@Builder
public record VipLevelOverviewVO(
        Integer currentLevel,
        List<VipLevelConfigVO> levels
) {}