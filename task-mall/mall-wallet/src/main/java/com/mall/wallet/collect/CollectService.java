package com.mall.wallet.collect;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.chain.address.DepositAddressService;
import com.mall.wallet.chain.model.entity.UserDepositAddress;
import com.mall.wallet.collect.enums.CollectStatus;
import com.mall.wallet.collect.mapper.CollectOrderMapper;
import com.mall.wallet.collect.model.entity.CollectOrder;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.payout.CollectPayout;
import com.mall.wallet.payout.PayoutService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * 归集编排器：扫描超阈值充值地址 → 建单 → 驱动归集状态机推进。自身不直接改订单状态——
 * 一切状态迁移委托 {@link CollectTxOps} 的原子方法。归集是平台内部资金搬运，<strong>全程不触发账务</strong>。
 * <p>
 * 状态机推进（{@link #advance} 逐笔幂等驱动，由调度器或手工触发）：
 * <pre>
 * CREATED ─ensureGas─┬─gas充足──► 归集广播 ─► SWEEPING ─确认达标─► COMPLETED(终态)
 *                    └─喂 gas──► GAS_FUNDING ─gas确认─► 归集广播 ─► SWEEPING
 * 任一广播失败 / 归集时余额空 ─────────────────────────────────► FAILED(终态)
 * </pre>
 * 安全：归集广播（喂 gas / 归集转账）不可回滚，置于事务外；广播后经 {@link CollectTxOps} 的 CAS 回填状态。
 * 「同地址至多一个进行中订单」由 {@link #scanAndCreate} 建单前校验保证，叠加 {@link PayoutService} 的
 * 「全余额归集（0则跳过）+ 喂 gas 前查 native」幂等基石，即便重复推进也不会重复转钱（第二次余额必空）。
 * 单地址 / 单订单异常被隔离，不影响同批其余。
 */
@Slf4j
@Service
public class CollectService {

    private static final String DEFAULT_COIN = "USDT";

    /** 进行中（非终态）的归集状态集合：建单去重与「进行中列表」查询共用。 */
    private static final List<String> ACTIVE_STATUSES = List.of(
            CollectStatus.CREATED.name(), CollectStatus.GAS_FUNDING.name(), CollectStatus.SWEEPING.name());

    @Resource
    private CollectProperties collectProperties;

    @Resource
    private ChainAdapterRegistry adapterRegistry;

    @Resource
    private DepositAddressService depositAddressService;

    @Resource
    private PayoutService payoutService;

    @Resource
    private CollectTxOps txOps;

    @Resource
    private CollectOrderMapper orderMapper;

    /**
     * 扫描某链所有充值地址，对「USDT 余额 ≥ 阈值且当前无进行中归集订单」的地址创建 CREATED 订单，返回新建订单数。
     * 链未配置归集规则 / 规则禁用 / 适配器不支持 → 直接返回 0。单地址异常被隔离记录，不影响其余地址。
     */
    public int scanAndCreate(ChainCode chainCode) {
        CollectProperties.ChainRule rule = collectProperties.rule(chainCode.name());
        if (rule == null || !rule.isEnabled() || !adapterRegistry.supports(chainCode)) {
            return 0;
        }
        int created = 0;
        for (UserDepositAddress addr : depositAddressService.listByChain(chainCode)) {
            try {
                if (hasActiveOrder(chainCode, addr.getAddress())) {
                    continue;
                }
                BigDecimal balance = payoutService.usdtBalance(chainCode, addr.getAddress());
                if (balance.compareTo(rule.getMinAmount()) < 0) {
                    continue;
                }
                txOps.create(newOrder(chainCode, addr, balance));
                created++;
                log.info("归集建单 chain={} addr={} balance={}", chainCode, addr.getAddress(), balance);
            } catch (Exception e) {
                log.warn("归集扫描单地址失败 chain={} addr={}", chainCode, addr.getAddress(), e);
            }
        }
        return created;
    }

    /**
     * 推进所有非终态归集订单一轮（调度器与手工触发共用）。逐笔隔离，单笔异常不扩散。
     */
    public void advanceAll() {
        for (String status : ACTIVE_STATUSES) {
            for (CollectOrder order : listByStatus(CollectStatus.valueOf(status))) {
                try {
                    advance(order);
                } catch (Exception e) {
                    log.warn("归集推进失败 order={}", order.getOrderNo(), e);
                }
            }
        }
    }

    /**
     * 推进单笔订单一步（幂等）。据当前状态：CREATED 判断喂 gas 或直接归集；GAS_FUNDING 等 gas 确认后归集；
     * SWEEPING 查归集确认数，达标置 COMPLETED，否则刷新确认数。终态 no-op。
     */
    public void advance(CollectOrder order) throws BizException {
        ChainCode chainCode = ChainCode.valueOf(order.getChainCode());
        int threshold = confirmThreshold(chainCode);
        switch (CollectStatus.valueOf(order.getStatus())) {
            case CREATED -> startSweepOrFundGas(order, chainCode);
            case GAS_FUNDING -> {
                long confs = adapterRegistry.get(chainCode).confirmations(order.getGasTxHash());
                if (confs >= threshold) {
                    // gas 到账，发起归集广播（来源态 GAS_FUNDING）
                    doSweep(order, chainCode, CollectStatus.GAS_FUNDING);
                }
            }
            case SWEEPING -> {
                long confs = adapterRegistry.get(chainCode).confirmations(order.getSweepTxHash());
                if (confs >= threshold) {
                    txOps.casCompleted(order.getOrderNo(), (int) confs);
                    log.info("归集确认达标 order={} confs={}", order.getOrderNo(), confs);
                } else {
                    txOps.refreshConfirmations(order.getOrderNo(), (int) confs);
                }
            }
            case COMPLETED, FAILED -> {
                // 终态，无动作
            }
        }
    }

    /** CREATED：按 native 余额决定喂 gas 还是直接归集。喂 gas / 起步异常 → 置 FAILED 留原因。 */
    private void startSweepOrFundGas(CollectOrder order, ChainCode chainCode) {
        try {
            Optional<String> gasTx = payoutService.ensureGas(chainCode, order.getFromAddress());
            if (gasTx.isPresent()) {
                txOps.casGasFunding(order.getOrderNo(), gasTx.get());
                log.info("归集喂 gas 已广播 order={} gasTx={}", order.getOrderNo(), gasTx.get());
            } else {
                // native 充足，跳过喂 gas，直接归集
                doSweep(order, chainCode, CollectStatus.CREATED);
            }
        } catch (BizException e) {
            txOps.casFailed(order.getOrderNo(), CollectStatus.CREATED, e.getMessage());
            log.warn("归集起步失败置 FAILED order={} reason={}", order.getOrderNo(), e.getMessage());
        }
    }

    /** 发起归集广播：把来源地址全部 USDT 转入出款热钱包。余额空 → FAILED；广播失败 → FAILED。 */
    private void doSweep(CollectOrder order, ChainCode chainCode, CollectStatus from) {
        try {
            String cipher = encPrivKeyOf(chainCode, order.getFromAddress());
            Optional<CollectPayout> payout = payoutService.collectAll(chainCode, order.getFromAddress(), cipher);
            if (payout.isEmpty()) {
                txOps.casFailed(order.getOrderNo(), from, "归集时余额为空");
                log.info("归集时余额为空置 FAILED order={}", order.getOrderNo());
                return;
            }
            CollectPayout p = payout.get();
            txOps.casSweeping(order.getOrderNo(), from, p.toAddress(), p.sweptAmount(), p.txHash());
            log.info("归集已广播 order={} amount={} tx={}", order.getOrderNo(), p.sweptAmount(), p.txHash());
        } catch (BizException e) {
            txOps.casFailed(order.getOrderNo(), from, e.getMessage());
            log.warn("归集广播失败置 FAILED order={} reason={}", order.getOrderNo(), e.getMessage());
        }
    }

    /** 某状态的归集订单（按 id 升序，先到先处理）。 */
    public List<CollectOrder> listByStatus(CollectStatus status) {
        return orderMapper.selectListByQuery(QueryWrapper.create()
                .from(CollectOrder.class)
                .eq(CollectOrder::getStatus, status.name())
                .orderBy(CollectOrder::getId, true));
    }

    /** 所有进行中订单（CREATED/GAS_FUNDING/SWEEPING，按 id 升序），供管理端监控。 */
    public List<CollectOrder> listActive() {
        return orderMapper.selectListByQuery(QueryWrapper.create()
                .from(CollectOrder.class)
                .in(CollectOrder::getStatus, ACTIVE_STATUSES)
                .orderBy(CollectOrder::getId, true));
    }

    private int confirmThreshold(ChainCode chainCode) {
        CollectProperties.ChainRule rule = collectProperties.rule(chainCode.name());
        return rule != null ? rule.getMinConfirmations() : Integer.MAX_VALUE;
    }

    private boolean hasActiveOrder(ChainCode chainCode, String address) {
        return orderMapper.selectCountByQuery(QueryWrapper.create()
                .from(CollectOrder.class)
                .eq(CollectOrder::getChainCode, chainCode.name())
                .eq(CollectOrder::getFromAddress, address)
                .in(CollectOrder::getStatus, ACTIVE_STATUSES)) > 0;
    }

    private String encPrivKeyOf(ChainCode chainCode, String address) throws BizException {
        UserDepositAddress addr = depositAddressService.findByChainAndAddress(chainCode, address);
        Preconditions.notNull(addr, WalletRespCodeEnum.DEPOSIT_ADDRESS_UNAVAILABLE, address);
        return addr.getEncPrivKey();
    }

    private CollectOrder newOrder(ChainCode chainCode, UserDepositAddress addr, BigDecimal balance) {
        CollectOrder order = new CollectOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(addr.getUserId());
        order.setChainCode(chainCode.name());
        order.setCoin(DEFAULT_COIN);
        order.setFromAddress(addr.getAddress());
        order.setAmount(balance);
        order.setStatus(CollectStatus.CREATED.name());
        order.setConfirmations(0);
        order.setRetryCount(0);
        return order;
    }

    private String nextOrderNo() {
        return "C" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}