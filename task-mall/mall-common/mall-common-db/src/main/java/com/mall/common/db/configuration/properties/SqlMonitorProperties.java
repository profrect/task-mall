package com.mall.common.db.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall.db.sql-monitor")
public class SqlMonitorProperties {

    /**
     * 慢sql阈值
     */
    private Long slowSqlThreshold;
}
