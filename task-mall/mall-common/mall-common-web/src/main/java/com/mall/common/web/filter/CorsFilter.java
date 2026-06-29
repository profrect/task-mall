package com.mall.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class CorsFilter extends AbstractFilter {

    public CorsFilter(List<String> skipPaths) {
        super(skipPaths);
    }

    @Override
    protected void filter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 允许所有来源
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 2. 不允许携带 Cookie
        response.setHeader("Access-Control-Allow-Credentials", "false");

        // 3. 允许的请求方法
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");

        // 4. 允许的自定义请求头
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Cache-Control");

        // 5. 预检请求的缓存时间（单位：秒），避免频繁发送 OPTIONS 请求
        response.setHeader("Access-Control-Max-Age", "3600");

        response.setHeader("Access-Control-Expose-Headers", "X-Trace-Id");

        // 如果是预检请求（OPTIONS），直接返回 200，不再往下执行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // 正常请求，继续执行后续链路
        filterChain.doFilter(request, response);
    }
}
