package com.mall.common.auth.configuration;

import cn.dev33.satoken.log.SaLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WebSaTokenConfiguration {

    /**
     * sa-token日志
     * @return saLog
     */
    @Bean
    @ConditionalOnClass(name = {"cn.dev33.satoken.log.SaLog"})
    public SaLog saLogForSlf4j(){
        return new SaLog() {
            @Override
            public void trace(String str, Object... args) {
                log.trace(str, args);
            }
            @Override
            public void debug(String str, Object... args) {
                log.debug(str, args);
            }
            @Override
            public void info(String str, Object... args) {
                log.info(str, args);
            }
            @Override
            public void warn(String str, Object... args) {
                log.warn(str, args);
            }
            @Override
            public void error(String str, Object... args) {
                log.error(str, args);
            }
            @Override
            public void fatal(String str, Object... args) {
                log.error(str, args);
            }
        };
    }
}
