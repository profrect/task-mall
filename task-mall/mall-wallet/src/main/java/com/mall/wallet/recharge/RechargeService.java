package com.mall.wallet.recharge;

import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.DepositEvent;
import com.mall.wallet.chain.address.DepositAddressService;
import com.mall.wallet.chain.mapper.ChainConfigMapper;
import com.mall.wallet.chain.mapper.ChainDepositEventMapper;
import com.mall.wallet.chain.mapper.CoinConfigMapper;
import com.mall.wallet.chain.model.entity.ChainConfig;
import com.mall.wallet.chain.model.entity.ChainDepositEvent;
import com.mall.wallet.chain.model.entity.CoinConfig;
import com.mall.wallet.chain.model.entity.UserDepositAddress;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.recharge.model.entity.WalletRechargeOrder;
import com.mall.wallet.recharge.mapper.WalletRechargeOrderMapper;
import com.mall.wallet.service.WalletAccountService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 充值入账编排。把"链上观测事件"推进为"用户余额入账"，全程幂等、可重复执行。
 * <p>
 * 分层与不变式：
 * - chain_deposit_event：infra 层"链上事实"，按 (chain,tx,logIndex) 去重，记录确认数演进。
 * - wallet_recharge_order：business 层"用户充值记录"，同键去重，供用户查询。
 * - wallet_flow_detail.uk_biz(RECHARGE, tx:logIndex)：资金入账幂等的最终底线（账务内核保证）。
 * 三层幂等叠加：即便扫块重复观测同一笔转账，余额也只增加一次。
 * <p>
 * 状态推进（同一事务内原子完成）：
 * 观测 → 写 event(SEEN)/order(CONFIRMING) → 确认达标 → applyLedger 入账 → event/order 置 CREDITED。
 */
@Slf4j
@Service
public class RechargeService {

    private static final String STATUS_SEEN = "SEEN";
    private static final String STATUS_CONFIRMING = "CONFIRMING";
    private static final String STATUS_CREDITED = "CREDITED";

    @Resource
    private ChainDepositEventMapper eventMapper;

    @Resource
    private WalletRechargeOrderMapper orderMapper;

    @Resource
    private ChainConfigMapper chainConfigMapper;

    @Resource
    private CoinConfigMapper coinConfigMapper;

    @Resource
    private DepositAddressService depositAddressService;

    @Resource
    private WalletAccountService walletAccountService;

    /**
     * 启用扫块的链配置；不存在或未启用返回 null。调度器据此决定是否扫描某链。
     */
    public ChainConfig getEnabledChainConfig(ChainCode chainCode) {
        ChainConfig cfg = chainConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(ChainConfig.class)
                .eq(ChainConfig::getChainCode, chainCode.name()));
        if (cfg == null || cfg.getEnabled() == null || cfg.getEnabled() != 1) {
            return null;
        }
        return cfg;
    }

    /**
     * 摄入一条链上充值观测事件，推进其状态机；达到确认门槛则入账。
     * 单事件单事务：失败只影响该事件，调度器记录后继续；下一轮扫块会重新摄入（幂等）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void ingest(ChainCode chainCode, DepositEvent ev, int minConfirmations) throws BizException {
        // 1) 关注的币种？（按观测到的合约地址匹配 coin_config，决定归一化精度）
        CoinConfig coin = findCoin(chainCode, ev.contractAddress());
        if (coin == null) {
            return;
        }
        // 2) 收款地址是否属于本平台某用户？非平台地址直接忽略
        UserDepositAddress owner = depositAddressService.findByChainAndAddress(chainCode, ev.toAddress());
        if (owner == null) {
            return;
        }
        // 3) 归一化金额：链上原始整数 / 10^decimals
        BigDecimal amount = normalize(ev.amountRaw(), coin.getDecimals());
        if (amount == null || amount.signum() <= 0) {
            return;
        }

        // 4) 链上事件去重/刷新；已入账则短路
        ChainDepositEvent event = upsertEvent(chainCode, ev);
        if (STATUS_CREDITED.equals(event.getStatus())) {
            return;
        }
        // 5) 用户充值订单去重/刷新
        WalletRechargeOrder order = upsertOrder(chainCode, ev, owner.getUserId(), coin.getSymbol(), amount);

        // 6) 确认门槛未达标：仅刷新确认数，等待下一轮
        if (ev.confirmations() < minConfirmations) {
            return;
        }

        // 7) 达标入账：账务内核 (RECHARGE, tx:logIndex) 幂等，重复调用不会重复加钱
        walletAccountService.getOrCreate(owner.getUserId(), coin.getSymbol());
        String bizId = ev.txHash() + ":" + ev.logIndex();
        walletAccountService.applyLedger(LedgerEvent.of(owner.getUserId(), WalletBizType.RECHARGE, bizId, amount));

        // 8) 置为已入账（同事务，与账务流水一致提交）
        long now = System.currentTimeMillis();
        order.setStatus(STATUS_CREDITED);
        order.setCreditedAt(now);
        orderMapper.update(order);

        event.setStatus(STATUS_CREDITED);
        eventMapper.update(event);

        log.info("充值入账完成 chain={} tx={} user={} amount={}", chainCode, bizId, owner.getUserId(), amount);
    }

    /**
     * 用户充值记录（按创建时间倒序）。
     */
    public List<WalletRechargeOrder> listUserRecharges(Long userId) {
        return orderMapper.selectListByQuery(QueryWrapper.create()
                .from(WalletRechargeOrder.class)
                .eq(WalletRechargeOrder::getUserId, userId)
                .orderBy(WalletRechargeOrder::getId, false));
    }

    /**
     * 管理端充值订单查询：status 留空查全部，否则按状态过滤；按 id 倒序（最新在前）并限量返回。
     * 充值订单主键只增不改，id 倒序等价于创建时间倒序，避免额外依赖 createTime 索引。
     */
    public List<WalletRechargeOrder> listForAdmin(String status, int limit) {
        QueryWrapper query = QueryWrapper.create().from(WalletRechargeOrder.class);
        if (status != null && !status.isBlank()) {
            query.eq(WalletRechargeOrder::getStatus, status.trim().toUpperCase(Locale.ROOT));
        }
        query.orderBy(WalletRechargeOrder::getId, false).limit(limit);
        return orderMapper.selectListByQuery(query);
    }

    private ChainDepositEvent upsertEvent(ChainCode chainCode, DepositEvent ev) {
        ChainDepositEvent event = findEvent(chainCode, ev.txHash(), ev.logIndex());
        if (event == null) {
            event = new ChainDepositEvent();
            event.setChainCode(chainCode.name());
            event.setContractAddress(ev.contractAddress());
            event.setTxHash(ev.txHash());
            event.setLogIndex(ev.logIndex());
            event.setFromAddress(ev.fromAddress());
            event.setToAddress(ev.toAddress());
            event.setAmountRaw(ev.amountRaw());
            event.setBlockHeight(ev.blockHeight());
            event.setConfirmations((int) ev.confirmations());
            event.setStatus(STATUS_SEEN);
            eventMapper.insert(event);
        } else {
            event.setConfirmations((int) ev.confirmations());
            event.setBlockHeight(ev.blockHeight());
            eventMapper.update(event);
        }
        return event;
    }

    private WalletRechargeOrder upsertOrder(ChainCode chainCode, DepositEvent ev, Long userId, String coin, BigDecimal amount) {
        WalletRechargeOrder order = findOrder(chainCode, ev.txHash(), ev.logIndex());
        if (order == null) {
            order = new WalletRechargeOrder();
            order.setOrderNo(nextOrderNo());
            order.setUserId(userId);
            order.setChainCode(chainCode.name());
            order.setCoin(coin);
            order.setAmount(amount);
            order.setFromAddress(ev.fromAddress());
            order.setToAddress(ev.toAddress());
            order.setTxHash(ev.txHash());
            order.setLogIndex(ev.logIndex());
            order.setConfirmations((int) ev.confirmations());
            order.setStatus(STATUS_CONFIRMING);
            orderMapper.insert(order);
        } else {
            order.setConfirmations((int) ev.confirmations());
            orderMapper.update(order);
        }
        return order;
    }

    private CoinConfig findCoin(ChainCode chainCode, String contractAddress) {
        if (contractAddress == null || contractAddress.isBlank()) {
            return null;
        }
        return coinConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(CoinConfig.class)
                .eq(CoinConfig::getChainCode, chainCode.name())
                .eq(CoinConfig::getContractAddress, contractAddress)
                .eq(CoinConfig::getEnabled, 1));
    }

    private ChainDepositEvent findEvent(ChainCode chainCode, String txHash, int logIndex) {
        return eventMapper.selectOneByQuery(QueryWrapper.create()
                .from(ChainDepositEvent.class)
                .eq(ChainDepositEvent::getChainCode, chainCode.name())
                .eq(ChainDepositEvent::getTxHash, txHash)
                .eq(ChainDepositEvent::getLogIndex, logIndex));
    }

    private WalletRechargeOrder findOrder(ChainCode chainCode, String txHash, int logIndex) {
        return orderMapper.selectOneByQuery(QueryWrapper.create()
                .from(WalletRechargeOrder.class)
                .eq(WalletRechargeOrder::getChainCode, chainCode.name())
                .eq(WalletRechargeOrder::getTxHash, txHash)
                .eq(WalletRechargeOrder::getLogIndex, logIndex));
    }

    private BigDecimal normalize(String amountRaw, Integer decimals) {
        try {
            int scale = decimals == null ? 0 : decimals;
            return new BigDecimal(amountRaw).movePointLeft(scale);
        } catch (NumberFormatException e) {
            log.warn("非法链上金额 amountRaw={}", amountRaw);
            return null;
        }
    }

    private String nextOrderNo() {
        return "R" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}