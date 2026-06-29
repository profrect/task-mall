package com.mall.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class AbstractFilter implements Filter {
    private final AntPathMatcher matcher = new AntPathMatcher();

    private List<String> skipPaths;

    public AbstractFilter(List<String> skipPaths) {
        this.skipPaths = skipPaths;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        if (uriMatch(requestURI)) {
            filterChain.doFilter(request, servletResponse);
            return;
        }
        // 具体的业务下放至对应的具体的filter
        filter(servletRequest, servletResponse, filterChain);
    }

    protected abstract void filter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException;

    private boolean uriMatch(String uri) {
        if (CollectionUtils.isEmpty(skipPaths)) {
            return false;
        }
        for (String path : skipPaths) {
            if (matcher.match(path, uri)) {
                return true;
            }
        }
        return false;
    }
}
