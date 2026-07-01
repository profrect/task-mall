package com.mall.mission.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 下游服务地址。任务中心只通过钱包 provider 触发奖励入账，不直接写钱包库。 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.service")
public class ServiceEndpointProperties {

    private String walletBaseUrl = "http://127.0.0.1:10002";
}