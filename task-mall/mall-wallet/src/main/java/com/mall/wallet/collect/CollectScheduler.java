package com.mall.wallet.collect;

import com.mall.wallet.chain.ChainCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 归集定时调度器。每轮先推进在途订单走完状态机，再扫描超阈值地址建新单。
 * <p>
 * 总开关默认<strong>关闭</strong>（{@code wallet.collect.enabled=false}）：归集是高敏感的链上资金搬运，
 * 默认仅允许人工接口触发；须显式开启才启动定时任务。关闭时调度器整体空转，在途订单可经管理端 /advance 手工推进。
 * <p>
 * 用 fixedDelay（非 fixedRate）：上一轮跑完才排下一轮，天然单线程串行——同一订单一轮至多推进一次，
 * 与 {@link PayoutService} 的「全余额归集 + 喂 gas 前查 native」幂等基石叠加，杜绝同订单并发重复广播。
 */
@Slf4j
@Component
public class CollectScheduler {

    @Resource
    private CollectService collectService;

    @Resource
    private CollectProperties collectProperties;

    @Scheduled(
            fixedDelayString = "${wallet.collect.scan-interval-ms:120000}",
            initialDelayString = "${wallet.collect.scan-initial-delay-ms:60000}")
    public void run() {
        if (!collectProperties.isEnabled()) {
            return;
        }
        // 先推进在途订单（让已建单走完 喂gas/归集/确认），再扫描建新单
        collectService.advanceAll();
        for (ChainCode chainCode : ChainCode.values()) {
            try {
                int created = collectService.scanAndCreate(chainCode);
                if (created > 0) {
                    log.info("归集扫描完成 chain={} created={}", chainCode, created);
                }
            } catch (Exception e) {
                log.warn("归集扫描失败 chain={}", chainCode, e);
            }
        }
    }
}