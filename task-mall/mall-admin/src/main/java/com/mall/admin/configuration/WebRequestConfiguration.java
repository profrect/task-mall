package com.mall.admin.configuration;

import com.mall.admin.configuration.properties.NoAuthUrlProperties;
import com.mall.admin.interceptor.AdminLoginInterceptor;
import com.mall.admin.interceptor.AdminPermissionInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(value = {NoAuthUrlProperties.class})
public class WebRequestConfiguration implements WebMvcConfigurer {

    @Resource
    private NoAuthUrlProperties noAuthUrlProperties;

    @Resource
    private AdminLoginInterceptor adminLoginInterceptor;

    @Resource
    private AdminPermissionInterceptor adminPermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/sys/admin/login")
                .excludePathPatterns(noAuthUrlProperties.getNoLoginUris());

        // 权限拦截器
        registry.addInterceptor(adminPermissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/sys/admin/login")
                .excludePathPatterns(noAuthUrlProperties.getNoPermUris());
    }
}

