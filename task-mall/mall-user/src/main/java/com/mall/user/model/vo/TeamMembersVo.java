package com.mall.user.model.vo;

import lombok.Builder;

@Builder
public record TeamMembersVo(
        String nickname,
        String vipLevel,
        Long invitateTime
) {
}
