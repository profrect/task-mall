package com.mall.promotion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.mall.promotion",
        "com.mall.common"
})
public class PromotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionApplication.class, args);
        log.info("---------------- PromotionApplication Start Success ----------------");
    }
}
