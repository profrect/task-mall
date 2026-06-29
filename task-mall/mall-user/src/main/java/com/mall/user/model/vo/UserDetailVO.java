package com.mall.user.model.vo;

import lombok.Builder;

@Builder
public record UserDetailVO(
        Long userId,
        String account,
        String nickName,
        String vipLevel,
        String inviteCode,
        String inviteUser,
        Integer teamMemberNum
) {}
