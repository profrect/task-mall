package com.mall.common.web.configuration;

import com.mall.common.core.cons.CommonConst;
import com.mall.common.core.util.JwtUtils;
import com.mall.common.web.rest.ApiRestClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient restClient(ApplicationProperties applicationProperties) {
        return RestClient.builder().requestInterceptor((request, body, execution) -> {
            String token = JwtUtils.generateTokenByDefaultKey(applicationProperties.getName(), null);
            request.getHeaders().add("X-Inner-Token", token);

            // 设置traceId
            String traceId = request.getHeaders().getFirst(CommonConst.HEADER_TRACE_ID);
            if (!StringUtils.hasText(traceId)) {
                traceId = MDC.get(CommonConst.LOG_TRACE_ID);
                if (!StringUtils.hasText(traceId)) {
                    traceId = UUID.randomUUID().toString().replace("-", "");
                }
            }
            request.getHeaders().add(CommonConst.HEADER_TRACE_ID, traceId);
            return execution.execute(request, body);
        }).build();
    }

    @Bean
    public ApiRestClient apiRestClient(RestClient restClient){
        return new ApiRestClient(restClient);
    }
}
