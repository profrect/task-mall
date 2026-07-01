package com.mall.wallet.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.resp.WalletStatsResp;
import com.mall.wallet.collect.mapper.CollectOrderMapper;
import com.mall.wallet.enums.LedgerDirection;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.mapper.WalletAccountMapper;
import com.mall.wallet.mapper.WalletFlowDetailMapper;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.model.entity.WalletAccount;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mall.wallet.recharge.mapper.WalletRechargeOrderMapper;
import com.mall.wallet.service.WalletAccountService;
import com.mall.wallet.withdraw.mapper.WalletWithdrawOrderMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * 账务内核实现。
 * <p>
 * 设计要点（对标 shop_java WalletService，但将其 13 个近似重复的资金方法坍缩为单一原语）：
 * 1) 唯一改余额入口 applyLedger，所有资金动作以 LedgerEvent 表达；
 * 2) 行锁 selectForUpdate 串行化同一 (用户,币种) 并发；
 * 3) 锁内幂等(bizType+bizId)，并以 uk_biz 唯一约束为崩溃兜底；
 * 4) 余额变换 mutate 为纯函数，账户不变式 total = avail + frozen 始终保持；
 * 5) 余额更新与流水写入处于同一事务，避免"改了余额却缺流水"。
 */
@Slf4j
@Service
public class WalletAccountServiceImpl extends ServiceImpl<WalletAccountMapper, WalletAccount>
        implements WalletAccountService {

    /** 阶段1单币种：USDT。 */
    private static final String DEFAULT_CURRENCY = "USDT";

    /** 账务标度：USDT 6 位小数。入账金额超过该精度直接判非法，绝不静默舍入。 */
    private static final int CURRENCY_SCALE = 6;

    @Resource
    private WalletFlowDetailMapper flowMapper;

    @Resource
    private WalletRechargeOrderMapper rechargeOrderMapper;

    @Resource
    private WalletWithdrawOrderMapper withdrawOrderMapper;

    @Resource
    private CollectOrderMapper collectOrderMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public WalletAccount getOrCreate(Long userId, String currency) throws BizException {
        Preconditions.notNull(userId, WalletRespCodeEnum.ACCOUNT_NOT_FOUND, "null");
        String ccy = normalizeCurrency(currency);

        WalletAccount acct = findAccount(userId, ccy);
        if (acct != null) {
            return acct;
        }
        // 首建：uk_user_currency 兜底并发首建，命中重复则回查既有账户
        try {
            WalletAccount fresh = newAccount(userId, ccy);
            save(fresh);
            return fresh;
        } catch (DuplicateKeyException e) {
            return findAccount(userId, ccy);
        }
    }

    @Override
    public List<WalletAccount> listByUserIds(List<Long> userIds, String currency) throws BizException {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return getMapper().selectListByQuery(QueryWrapper.create().from(WalletAccount.class)
                .in(WalletAccount::getUserId, userIds)
                .eq(WalletAccount::getCurrency, normalizeCurrency(currency)));
    }

    @Override
    public WalletStatsResp stats(String currency, long todayStartAt) throws BizException {
        String ccy = normalizeCurrency(currency);
        WalletStatsResp resp = new WalletStatsResp();
        resp.setWalletAccounts(nz(getMapper().countByCurrency(ccy)));
        resp.setTotalBalance(nz(getMapper().sumTotalBalance(ccy)));
        resp.setAvailableBalance(nz(getMapper().sumAvailBalance(ccy)));
        resp.setFrozenBalance(nz(getMapper().sumFrozenBalance(ccy)));

        resp.setRechargeOrders(nz(rechargeOrderMapper.countByCoin(ccy)));
        resp.setRechargeAmount(nz(rechargeOrderMapper.sumCreditedAmount(ccy)));
        resp.setTodayRechargeOrders(nz(rechargeOrderMapper.countToday(ccy, todayStartAt)));
        resp.setTodayRechargeAmount(nz(rechargeOrderMapper.sumTodayCreditedAmount(ccy, todayStartAt)));

        resp.setWithdrawOrders(nz(withdrawOrderMapper.countByCoin(ccy)));
        resp.setWithdrawAmount(nz(withdrawOrderMapper.sumApprovedAmount(ccy)));
        resp.setReviewingWithdrawOrders(nz(withdrawOrderMapper.countReviewing(ccy)));
        resp.setTodayWithdrawOrders(nz(withdrawOrderMapper.countToday(ccy, todayStartAt)));
        resp.setTodayWithdrawAmount(nz(withdrawOrderMapper.sumTodayApprovedAmount(ccy, todayStartAt)));

        resp.setCollectOrders(nz(collectOrderMapper.countByCoin(ccy)));
        resp.setCollectedAmount(nz(collectOrderMapper.sumCompletedAmount(ccy)));
        resp.setActiveCollectOrders(nz(collectOrderMapper.countActive(ccy)));
        resp.setTodayCollectOrders(nz(collectOrderMapper.countToday(ccy, todayStartAt)));
        resp.setTodayCollectedAmount(nz(collectOrderMapper.sumTodayCompletedAmount(ccy, todayStartAt)));
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public WalletFlowDetail applyLedger(LedgerEvent event) throws BizException {
        BigDecimal amount = normalizeAmount(event.amount());
        String ccy = normalizeCurrency(event.currency());

        // 行锁：串行化同一 (用户,币种) 的并发资金操作
        WalletAccount acct = getMapper().selectForUpdate(event.userId(), ccy);
        Preconditions.notNull(acct, WalletRespCodeEnum.ACCOUNT_NOT_FOUND, event.userId());

        // 锁内幂等：同一 (bizType,bizId) 只入账一次；命中则返回既有流水
        WalletFlowDetail existing = findFlow(event.bizType(), event.bizId());
        if (existing != null) {
            return existing;
        }

        BigDecimal availBefore = acct.getAvailBalance();
        mutate(acct, event.bizType().getDirection(), amount);
        getMapper().update(acct);

        WalletFlowDetail flow = buildFlow(event, acct, amount, availBefore);
        flowMapper.insert(flow);
        return flow;
    }

    /**
     * 纯余额变换 + 前置校验。账户不变式 total = avail + frozen 在每个分支都保持。
     */
    private void mutate(WalletAccount a, LedgerDirection dir, BigDecimal amt) throws BizException {
        switch (dir) {
            case IN -> {
                a.setAvailBalance(a.getAvailBalance().add(amt));
                a.setTotalBalance(a.getTotalBalance().add(amt));
            }
            case OUT -> {
                Preconditions.needTrue(a.getAvailBalance().compareTo(amt) >= 0, WalletRespCodeEnum.INSUFFICIENT_BALANCE);
                a.setAvailBalance(a.getAvailBalance().subtract(amt));
                a.setTotalBalance(a.getTotalBalance().subtract(amt));
            }
            case FREEZE -> {
                Preconditions.needTrue(a.getAvailBalance().compareTo(amt) >= 0, WalletRespCodeEnum.INSUFFICIENT_BALANCE);
                a.setAvailBalance(a.getAvailBalance().subtract(amt));
                a.setFrozenBalance(a.getFrozenBalance().add(amt));
            }
            case UNFREEZE -> {
                Preconditions.needTrue(a.getFrozenBalance().compareTo(amt) >= 0, WalletRespCodeEnum.FROZEN_BALANCE_INVALID);
                a.setFrozenBalance(a.getFrozenBalance().subtract(amt));
                a.setAvailBalance(a.getAvailBalance().add(amt));
            }
            case SETTLE -> {
                Preconditions.needTrue(a.getFrozenBalance().compareTo(amt) >= 0, WalletRespCodeEnum.FROZEN_BALANCE_INVALID);
                a.setFrozenBalance(a.getFrozenBalance().subtract(amt));
                a.setTotalBalance(a.getTotalBalance().subtract(amt));
            }
            default -> throw new IllegalStateException("unsupported ledger direction: " + dir);
        }
    }

    private WalletFlowDetail buildFlow(LedgerEvent ev, WalletAccount acct, BigDecimal amount, BigDecimal availBefore) {
        WalletFlowDetail flow = new WalletFlowDetail();
        flow.setFlowNo(nextFlowNo());
        flow.setUserId(acct.getUserId());
        flow.setWalletId(acct.getId());
        flow.setBizType(ev.bizType().name());
        flow.setBizId(ev.bizId());
        flow.setDirection(ev.bizType().getDirection().name());
        flow.setChangeAmt(amount);
        flow.setBalanceBefore(availBefore);
        flow.setBalanceAfter(acct.getAvailBalance());
        flow.setRemark(StringUtils.hasText(ev.remark()) ? ev.remark() : ev.bizType().getDefaultRemark());
        return flow;
    }

    private WalletAccount findAccount(Long userId, String currency) {
        return getMapper().selectOneByQuery(QueryWrapper.create().from(WalletAccount.class)
                .eq(WalletAccount::getUserId, userId)
                .eq(WalletAccount::getCurrency, currency));
    }

    private WalletFlowDetail findFlow(WalletBizType bizType, String bizId) {
        return flowMapper.selectOneByQuery(QueryWrapper.create().from(WalletFlowDetail.class)
                .eq(WalletFlowDetail::getBizType, bizType.name())
                .eq(WalletFlowDetail::getBizId, bizId));
    }

    private WalletAccount newAccount(Long userId, String currency) {
        WalletAccount a = new WalletAccount();
        a.setUserId(userId);
        a.setCurrency(currency);
        a.setTotalBalance(BigDecimal.ZERO);
        a.setAvailBalance(BigDecimal.ZERO);
        a.setFrozenBalance(BigDecimal.ZERO);
        return a;
    }

    private String normalizeCurrency(String currency) {
        return StringUtils.hasText(currency) ? currency : DEFAULT_CURRENCY;
    }

    private Long nz(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) throws BizException {
        Preconditions.needTrue(amount != null && amount.signum() > 0, WalletRespCodeEnum.INVALID_AMOUNT, CURRENCY_SCALE);
        Preconditions.needTrue(amount.scale() <= CURRENCY_SCALE, WalletRespCodeEnum.INVALID_AMOUNT, CURRENCY_SCALE);
        // 已校验标度不超过 6，setScale 用 UNNECESSARY 不会触发舍入
        return amount.setScale(CURRENCY_SCALE, RoundingMode.UNNECESSARY);
    }

    private String nextFlowNo() {
        return "F" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}