package com.mall.user.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(
        @NotBlank(message = "{params.account.notBlank}")
        String account,

        @NotBlank(message = "{params.password.notBlank}")
        String password
) {}
