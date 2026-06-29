package com.mall.common.web.filter;

import com.mall.common.core.cons.CommonConst;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 请求参数打印 filter
 *
 * @author ADM
 */
@Slf4j
public class RequestParamFilter extends OncePerRequestFilter {
    private static final int MAX_LOG_BODY_LENGTH = 1024 * 32;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_LOG_BODY_LENGTH);
        try {
            filterChain.doFilter(requestWrapper, response);
        } finally {
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, MAX_LOG_BODY_LENGTH);
                String body = new String(buf, 0, length, requestWrapper.getCharacterEncoding());
                String traceId = MDC.get(CommonConst.LOG_TRACE_ID);
                log.info("---->> [{}]-[method={}, uri={}], Params:{}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        body
                );
            }
        }
    }
}
