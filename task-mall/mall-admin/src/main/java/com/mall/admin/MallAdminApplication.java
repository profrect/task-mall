package com.mall.admin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.mall.admin",
        "com.mall.common"
})
@MapperScan(basePackages = {"com.mall.admin.mapper"})
public class MallAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAdminApplication.class, args);
        log.info("---------------- MallAdminApplication Start Success ----------------");
    }
}
