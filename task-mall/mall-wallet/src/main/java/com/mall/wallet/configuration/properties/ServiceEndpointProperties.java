package com.mall.wallet.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 下游服务地址（阶段1无注册中心，mall-wallet 仅用于必要的用户存在性校验）。 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.service")
public class ServiceEndpointProperties {

    private String userBaseUrl = "http://127.0.0.1:10001";
}