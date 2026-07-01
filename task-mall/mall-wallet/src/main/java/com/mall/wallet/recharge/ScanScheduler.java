package com.mall.wallet.recharge;

import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.chain.adapter.DepositEvent;
import com.mall.wallet.chain.address.DepositAddressService;
import com.mall.wallet.chain.model.entity.ChainConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 充值扫块调度器。周期性拉取各启用链充值地址池上的新转入，逐笔交给 {@link RechargeService} 入账。
 * <p>
 * 隔离策略（失败不扩散）：
 * - 链级 try/catch：某条链扫块异常不影响其它链。
 * - 事件级 try/catch：单笔事件入账失败只记录日志，不中断同批其它事件；下一轮幂等重试。
 * 用 fixedDelay（非 fixedRate）：上一轮跑完才排下一轮，天然避免同一地址池被并发摄入。
 */
@Slf4j
@Component
public class ScanScheduler {

    private final ChainAdapterRegistry registry;
    private final DepositAddressService depositAddressService;
    private final RechargeService rechargeService;

    public ScanScheduler(ChainAdapterRegistry registry,
                         DepositAddressService depositAddressService,
                         RechargeService rechargeService) {
        this.registry = registry;
        this.depositAddressService = depositAddressService;
        this.rechargeService = rechargeService;
    }

    @Scheduled(
            fixedDelayString = "${wallet.scan.interval-ms:30000}",
            initialDelayString = "${wallet.scan.initial-delay-ms:15000}")
    public void scan() {
        for (ChainCode chainCode : registry.supportedChains()) {
            try {
                ChainConfig cfg = rechargeService.getEnabledChainConfig(chainCode);
                if (cfg == null) {
                    continue;
                }
                List<String> watchAddresses = depositAddressService.listWatchAddresses(chainCode);
                if (watchAddresses.isEmpty()) {
                    continue;
                }
                int minConfirmations = cfg.getMinConfirmations() == null
                        ? Integer.MAX_VALUE : cfg.getMinConfirmations();

                List<DepositEvent> events = registry.get(chainCode).scanDeposits(watchAddresses);
                for (DepositEvent ev : events) {
                    try {
                        rechargeService.ingest(chainCode, ev, minConfirmations);
                    } catch (Exception e) {
                        log.error("充值事件入账失败 chain={} tx={}:{}", chainCode, ev.txHash(), ev.logIndex(), e);
                    }
                }
            } catch (Exception e) {
                log.error("链扫块失败 chain={}", chainCode, e);
            }
        }
    }
}