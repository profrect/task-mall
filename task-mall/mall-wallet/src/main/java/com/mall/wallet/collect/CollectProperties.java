package com.mall.wallet.collect;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 归集配置（prefix=wallet.collect）。
 * <p>
 * 归集 = 把分散在各用户充值地址上的 USDT 集中到出款热钱包，便于统一出款与对账。
 * 全局开关默认\u003cstrong\u003e关闭\u003c/strong\u003e：归集是高敏感的链上资金搬运，默认仅允许人工触发，
 * 定时归集须显式 enabled=true 才启动（且每链仍受 chains.*.enabled 二级开关约束）。
 */
@Data
@ConfigurationProperties(prefix = "wallet.collect")
public class CollectProperties {

    /** 定时归集总开关：默认 false（仅手工接口触发，定时扫描不工作）。 */
    private boolean enabled = false;

    /** 每链归集规则，key = ChainCode 名。 */
    private Map<String, ChainRule> chains = new HashMap<>();

    /** 取某链规则，未配置返回 null（调用方据此判定「该链未配置归集」）。 */
    public ChainRule rule(String chainCode) {
        return chains.get(chainCode);
    }

    @Data
    public static class ChainRule {

        /** 该链是否允许归集（定时与手工批量均受此约束；手工指定单地址可绕过由接口决定）。 */
        private boolean enabled = true;

        /** 归集阈值：地址 USDT 余额高于此值才归集，避免小额归集 gas 不划算（币种单位）。 */
        private BigDecimal minAmount = new BigDecimal("10");

        /** gas 代付 / 归集交易的确认数达标阈值（状态机推进依据）。 */
        private int minConfirmations = 19;
    }
}