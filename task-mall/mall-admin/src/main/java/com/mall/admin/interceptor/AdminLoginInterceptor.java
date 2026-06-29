package com.mall.admin.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 用户登录拦截器
 */
@Component
@Slf4j
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查登录，如果没有登录，则会抛出 NotLoginException 异常
        Object loginId;
        try {
            loginId = StpUtil.getLoginId();
        } catch (NotLoginException ex) {
            log.info("--->> 用户认证失败：", ex);
            throw new BizException(CommonRespCode.AUTH_DENIED, null);
        }
        // 获取tokensession
        SaSession tokenSession = StpUtil.getTokenSession();
        UserContext.User user = (UserContext.User) tokenSession.get(loginId.toString());
        if(Objects.isNull(user)) {
            // 没有获取到用户信息，有可能是因为内存不足，redis进行了数据驱逐，此时应该让用户重新登录，或者直接查询数据库
            String loginType = StpUtil.getLoginType();
            StpUtil.logout(loginId);
            String msg = "账号【%s】，没有找到缓存的用户信息，需要重新登录";
            throw new NotLoginException(String.format(msg, loginId), loginType, "");
        }
        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}

