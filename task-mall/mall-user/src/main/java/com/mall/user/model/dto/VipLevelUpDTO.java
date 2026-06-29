package com.mall.user.model.dto;

import jakarta.validation.constraints.NotNull;

public record VipLevelUpDTO(
        @NotNull(message = "等级不能为空")
        Integer level,

        @NotNull(message = "资金密码不能为空")
        String fundPassword
) {
}
