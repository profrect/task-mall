package com.mall.common.auth.util;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;

/**
 * 登录态解析：统一从 sa-token 取当前登录用户的全局 userId。
 * <p>
 * 约定：各服务登录时 loginId = 全局 userId（见 mall-user 登录流程）。
 * sa-token 会话存于共享 Redis，因此任意下游服务（如 mall-wallet）都能凭同一 token 解析出 userId，
 * 无需 user → 下游的强耦合调用。
 */
public final class AuthUtils {

    private AuthUtils() {
    }

    /**
     * 当前登录用户的全局 userId；未登录则抛 AUTH_DENIED。
     */
    public static Long currentUserId() throws BizException {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (NotLoginException ex) {
            throw new BizException(CommonRespCode.AUTH_DENIED, null);
        }
    }
}