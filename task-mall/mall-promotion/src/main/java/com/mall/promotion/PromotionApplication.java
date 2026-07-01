package com.mall.promotion;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.mall.promotion",
        "com.mall.common"
})
@MapperScan(basePackages = {"com.mall.promotion.mapper"})
public class PromotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionApplication.class, args);
        log.info("---------------- PromotionApplication Start Success ----------------");
    }
}
