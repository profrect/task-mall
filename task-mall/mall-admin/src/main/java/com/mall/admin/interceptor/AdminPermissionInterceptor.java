package com.mall.admin.interceptor;

import cn.dev33.satoken.exception.NotPermissionException;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 用户权限拦截器
 */
@Component
@Slf4j
public class AdminPermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserContext.User user = UserContext.current();
        if(user.superPermission()) {
            // 拥有超级管理员用户组，则跳过权限检查
            return true;
        }

        // 不是超级管理用户组，则检查权限
        if(handler instanceof HandlerMethod hm) {
            Method method = hm.getMethod();
            Collection<String> roleApis = user.roleApis();
            if(roleApis.contains(method.toString())) {
                return true;
            }
        }
        log.info("--->> 用户操作权限不足");
        throw new BizException(CommonRespCode.PERMISSION_DENIED, null);
    }
}
