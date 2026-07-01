package com.mall.user.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 下游服务地址（阶段1无注册中心，mall-user 仅在资金动作时调用钱包 provider）。 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.service")
public class ServiceEndpointProperties {

    private String walletBaseUrl = "http://127.0.0.1:10002";
}