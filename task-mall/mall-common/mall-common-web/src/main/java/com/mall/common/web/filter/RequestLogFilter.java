package com.mall.common.web.filter;

import com.mall.common.core.cons.CommonConst;
import com.mall.common.web.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

/**
 * 请求日志打印 filter
 *
 * @author ADM
 */
@Slf4j
public class RequestLogFilter extends AbstractFilter {

    public RequestLogFilter(List<String> skipPaths) {
        super(skipPaths);
    }

    @Override
    protected void filter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceId = MDC.get(CommonConst.LOG_TRACE_ID);
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long end = System.currentTimeMillis();
            log.info("---->> [{}][{}]-[method={}, uri={}, status={}, cost={}ms]",
                    IpUtils.getRealIp(request),
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    end - start
            );
        }
    }
}
