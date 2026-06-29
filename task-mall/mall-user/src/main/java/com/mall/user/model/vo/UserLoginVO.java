package com.mall.user.model.vo;

import lombok.Builder;

@Builder
public record UserLoginVO(
        String accessToken,
        Long expiresIn,
        String tokenType
) {}
