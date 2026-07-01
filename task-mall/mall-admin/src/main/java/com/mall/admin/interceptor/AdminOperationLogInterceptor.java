package com.mall.admin.interceptor;

import com.mall.admin.service.audit.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 后台操作审计拦截器。 */
@Component
public class AdminOperationLogInterceptor implements HandlerInterceptor {

    private static final String START_AT = AdminOperationLogInterceptor.class.getName() + ".START_AT";

    @Resource
    private OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_AT, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!shouldRecord(request)) {
            return;
        }
        long durationMs = System.currentTimeMillis() - startAt(request);
        operationLogService.record(request, response.getStatus(), durationMs);
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/admin/") || path.equals("/api/sys/admin/logout");
    }

    private long startAt(HttpServletRequest request) {
        Object value = request.getAttribute(START_AT);
        return value instanceof Long startAt ? startAt : System.currentTimeMillis();
    }
}