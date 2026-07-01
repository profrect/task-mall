package com.mall.wallet.withdraw;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 提现规则配置（prefix=wallet.withdraw）。
 * <p>
 * 全局开关 + 每链规则（最小额 / 手续费 / 确认数）。手续费为固定额（币种单位），平台留存：
 * 链上实际转出 = 申请额 - 手续费，差额留在热钱包作为平台收入（不拆为用户账变）。
 */
@Data
@ConfigurationProperties(prefix = "wallet.withdraw")
public class WithdrawProperties {

    /** 提现总开关：关闭时所有链均不可提现。 */
    private boolean enabled = true;

    /** 每链提现规则，key = ChainCode 名。 */
    private Map<String, ChainRule> chains = new HashMap<>();

    /** 取某链规则，未配置返回 null（调用方据此判定"该链不支持提现"）。 */
    public ChainRule rule(String chainCode) {
        return chains.get(chainCode);
    }

    @Data
    public static class ChainRule {

        /** 该链是否允许提现。 */
        private boolean enabled = true;

        /** 最小提现额（币种单位，如 USDT）。 */
        private BigDecimal minAmount = new BigDecimal("10");

        /** 固定手续费（币种单位），平台留存。 */
        private BigDecimal fee = new BigDecimal("1");

        /** 出账确认数达标阈值。 */
        private int minConfirmations = 19;
    }
}