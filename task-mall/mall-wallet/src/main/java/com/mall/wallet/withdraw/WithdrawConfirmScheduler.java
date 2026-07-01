package com.mall.wallet.withdraw;

import com.mall.wallet.withdraw.enums.WithdrawStatus;
import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 提现确认轮询调度器。周期性扫描 BROADCASTING 订单，逐笔查询链上确认数，达标即结算。
 * <p>
 * 隔离策略（失败不扩散）：单订单 try/catch，某笔确认查询/结算异常不影响同批其余；下一轮幂等重试。
 * 用 fixedDelay（非 fixedRate）：上一轮跑完才排下一轮，避免同批订单被并发重复轮询。
 */
@Slf4j
@Component
public class WithdrawConfirmScheduler {

    @Resource
    private WithdrawService withdrawService;

    @Scheduled(
            fixedDelayString = "${wallet.withdraw.confirm-interval-ms:30000}",
            initialDelayString = "${wallet.withdraw.confirm-initial-delay-ms:20000}")
    public void scan() {
        List<WalletWithdrawOrder> broadcasting = withdrawService.listByStatus(WithdrawStatus.BROADCASTING);
        for (WalletWithdrawOrder order : broadcasting) {
            try {
                withdrawService.pollConfirm(order);
            } catch (Exception e) {
                log.warn("提现确认轮询失败 order={}", order.getOrderNo(), e);
            }
        }
    }
}