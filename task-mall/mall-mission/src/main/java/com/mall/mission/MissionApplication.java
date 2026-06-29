package com.mall.mission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.mall.mission",
        "com.mall.common"
})
public class MissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MissionApplication.class, args);
        log.info("---------------- MissionApplication Start Success ----------------");
    }
}
