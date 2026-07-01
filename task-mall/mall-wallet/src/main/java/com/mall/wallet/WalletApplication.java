package com.mall.wallet;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.mall.wallet",
        "com.mall.common"
})
@MapperScan(basePackages = {
        "com.mall.wallet.mapper",
        "com.mall.wallet.chain.mapper",
        "com.mall.wallet.recharge.mapper",
        "com.mall.wallet.withdraw.mapper",
        "com.mall.wallet.collect.mapper",
        "com.mall.wallet.transfer.mapper",
        "com.mall.wallet.payment.mapper"
})
public class WalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
        log.info("---------------- WalletApplication Start Success ----------------");
    }
}
