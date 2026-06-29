package com.mall.common.db.configuration;


import com.mall.common.db.configuration.properties.SqlMonitorProperties;
import com.mall.common.db.monitor.MybatisFlexSqlMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {SqlMonitorProperties.class})
public class SqlMonitorConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mall.db.sql-monitor", name = "enable", havingValue = "true", matchIfMissing = false)
    public MybatisFlexSqlMonitor mybatisFlexSqlMonitor(SqlMonitorProperties sqlMonitorProperties){
        Long slowSqlThreshold = sqlMonitorProperties.getSlowSqlThreshold();
        if(slowSqlThreshold == null) {
            slowSqlThreshold = 1500L;
        }
        return new MybatisFlexSqlMonitor(slowSqlThreshold);
    }
}
