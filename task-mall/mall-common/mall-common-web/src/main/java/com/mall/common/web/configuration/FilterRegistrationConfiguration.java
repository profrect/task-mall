package com.mall.common.web.configuration;

import com.mall.common.web.filter.CorsFilter;
import com.mall.common.web.filter.MdcFilter;
import com.mall.common.web.filter.RequestLogFilter;
import com.mall.common.web.filter.RequestParamFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class FilterRegistrationConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mall.web-filter.cors", name = "enable", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter(Collections.emptyList()));
        registration.addUrlPatterns("/*");
        registration.setOrder(-10);
        registration.setName("mallWebCorsFilter");
        registration.setEnabled(true);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "mall.web-filter.mdc", name = "enable", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<MdcFilter> mdcFilter() {
        FilterRegistrationBean<MdcFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MdcFilter(Collections.emptyList()));
        registration.addUrlPatterns("/*");
        registration.setOrder(-9);
        registration.setName("mdcFilter");
        registration.setEnabled(true);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "mall.web-filter.request-log", name = "enable", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RequestLogFilter> requestLogFilter() {
        FilterRegistrationBean<RequestLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLogFilter(Collections.emptyList()));
        registration.addUrlPatterns("/*");
        registration.setOrder(-8);
        registration.setName("requestLogFilter");
        registration.setEnabled(true);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "mall.web-filter.request-param", name = "enable", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<RequestParamFilter> requestParamFilter() {
        FilterRegistrationBean<RequestParamFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestParamFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-7);
        registration.setName("requestParamFilter");
        registration.setEnabled(true);
        return registration;
    }
}
