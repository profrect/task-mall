package com.mall.admin.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 下游服务地址（阶段1：无注册中心，硬编码 IP 直连）。
 * <p>
 * 地址配置化，集中消除散落在代码里的写死地址（含修正历史 stub 里 user 端口 12000 的错配）。
 * 引入网关 / 注册中心后，这些直连基址将由服务发现取代。
 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.service")
public class ServiceEndpointProperties {

    /** mall-wallet 基址，默认本机 10002。 */
    private String walletBaseUrl = "http://127.0.0.1:10002";

    /** mall-user 基址，默认本机 10001。 */
    private String userBaseUrl = "http://127.0.0.1:10001";

    /** mall-mission 基址，默认本机 10003。 */
    private String missionBaseUrl = "http://127.0.0.1:10003";

    /** mall-promotion 基址，默认本机 10004。 */
    private String promotionBaseUrl = "http://127.0.0.1:10004";
}