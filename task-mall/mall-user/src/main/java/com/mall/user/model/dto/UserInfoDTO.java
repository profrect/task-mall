package com.mall.user.model.dto;

import com.mall.common.core.valid.StrLen;

/**
 * 用户资料更新入参。
 * <p>
 * 刻意不含 userId——目标用户一律由 sa-token 从会话解析，绝不接受前端传入，杜绝改他人资料的越权。
 * 仅暴露可由用户自助变更的字段（昵称 / 邮箱）；vipLevel/status/inviteCode 等受控字段不在此列。
 */
public record UserInfoDTO(
        @StrLen(maxLen = 100, message = "email太长")
        String email,
        @StrLen(maxLen = 50, message = "nickname太长")
        String nickname
) {
}
