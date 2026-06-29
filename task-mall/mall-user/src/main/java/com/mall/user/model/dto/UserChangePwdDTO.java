package com.mall.user.model.dto;

import com.mall.common.core.valid.StrLen;
import jakarta.validation.constraints.NotBlank;

public record UserChangePwdDTO(
        @NotBlank(message = "旧密码不能为空")
        String origPassword,

        @NotBlank(message = "新密码不能为空")
        @StrLen(maxLen = 50, message = "新密码太长")
        String newPassword
) {
}
