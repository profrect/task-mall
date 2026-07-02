package com.mall.common.auth.util;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
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

    public static final String IMPERSONATED_KEY = "mall:impersonated";
    public static final String IMPERSONATION_ADMIN_KEY = "mall:impersonation:admin";
    public static final String IMPERSONATION_TARGET_USER_KEY = "mall:impersonation:targetUserId";
    public static final String IMPERSONATION_TICKET_ID_KEY = "mall:impersonation:ticketId";

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

    /**
     * 标记当前 token 为后台模拟登录会话。写入 token session，跨服务共享读取。
     */
    public static void markImpersonation(String adminAccount, Long targetUserId, Long ticketId) {
        SaSession session = StpUtil.getTokenSession();
        session.set(IMPERSONATED_KEY, Boolean.TRUE);
        session.set(IMPERSONATION_ADMIN_KEY, adminAccount);
        session.set(IMPERSONATION_TARGET_USER_KEY, targetUserId);
        session.set(IMPERSONATION_TICKET_ID_KEY, ticketId);
    }

    /**
     * 当前 token 是否后台模拟登录态；未登录按 false 处理，具体登录校验仍由 currentUserId 完成。
     */
    public static boolean isImpersonated() {
        try {
            Object value = StpUtil.getTokenSession().get(IMPERSONATED_KEY);
            return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
        } catch (NotLoginException ex) {
            return false;
        }
    }

    /**
     * 前台写操作统一保护：后台模拟登录只允许查看，不允许产生业务状态变更。
     */
    public static void ensureNotImpersonated() throws BizException {
        if (isImpersonated()) {
            throw new BizException(CommonRespCode.IMPERSONATION_READONLY, null);
        }
    }
}