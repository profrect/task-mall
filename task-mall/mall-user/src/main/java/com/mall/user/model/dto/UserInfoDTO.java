package com.mall.user.model.dto;

import com.mall.common.core.valid.StrLen;
import jakarta.validation.constraints.NotNull;

public record UserInfoDTO(
        @NotNull(message = "userId不能为空")
        Long userId,
        @StrLen(maxLen = 100, message = "email太长")
        String email,
        @StrLen(maxLen = 50, message = "nickname太长")
        String nickname
) {
}
