package com.mall.user.model.dto;

import com.mall.common.core.valid.StrLen;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDTO(
        @NotBlank(message = "{params.account.notBlank}")
        @StrLen(maxLen = 3, message = "{params.account.length.exceeded}")
        String account,

        @NotBlank(message = "{params.password.notBlank}")
        @StrLen(maxLen = 50, message = "{params.password.length.exceeded}")
        String password,

        @NotBlank(message = "{params.email.notBlank}")
        @StrLen(maxLen = 100, message = "{params.email.length.exceeded}")
        String email,

        @StrLen(maxLen = 50, message = "{params.nickName.length.exceeded}")
        String nickname,

        String inviteCode
) {
}
