package com.mall.wallet.withdraw;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.payout.PayoutResult;
import com.mall.wallet.payout.PayoutService;
import com.mall.wallet.withdraw.enums.WithdrawStatus;
import com.mall.wallet.withdraw.mapper.WalletWithdrawOrderMapper;
import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 提现编排器：校验 + 流程编排 + 查询，自身不直接改余额、不直接改订单状态——
 * 一切状态机迁移委托给 {@link WithdrawTxOps} 的原子方法，从而把"业务流程"与"原子事务"两个关注点分离。
 * <p>
 * 流程：
 * <pre>
 * apply   : 校验 → openFrozen(落单 REVIEWING + 冻结)
 * approve : casApprove(REVIEWING→APPROVED) → 热钱包广播(事务外) → casBroadcasting / casFailedFromApproved
 * reject  : casReject(REVIEWING→REJECTED + 解冻)
 * confirm : pollConfirm → 达标 casConfirmed(BROADCASTING→CONFIRMED + 结算)
 * </pre>
 * 关键：approve 的链上广播不可回滚，故 approve 本身<strong>非事务</strong>，广播前后各自走 TxOps 的独立原子事务，
 * 并以 CAS 保证"至多一次广播"。
 */
@Slf4j
@Service
public class WithdrawService {

    private static final String DEFAULT_COIN = "USDT";

    @Resource
    private WithdrawProperties withdrawProperties;

    @Resource
    private ChainAdapterRegistry adapterRegistry;

    @Resource
    private PayoutService payoutService;

    @Resource
    private WithdrawTxOps txOps;

    @Resource
    private WalletWithdrawOrderMapper orderMapper;

    /**
     * 用户申请提现：校验链/开关/地址/金额 → 计算手续费与到账额 → 冻结并落单(REVIEWING)，等待人工审核。
     * 金额标度(≤6)校验由账务内核在冻结时把关，非法标度会令整笔回滚、不留订单。
     */
    public WalletWithdrawOrder apply(Long userId, ChainCode chainCode, String coin,
                                     String toAddress, BigDecimal amount) throws BizException {
        Preconditions.needTrue(withdrawProperties.isEnabled(), WalletRespCodeEnum.WITHDRAW_DISABLED);
        WithdrawProperties.ChainRule rule = withdrawProperties.rule(chainCode.name());
        Preconditions.needTrue(rule != null && rule.isEnabled(), WalletRespCodeEnum.WITHDRAW_DISABLED);
        Preconditions.needTrue(adapterRegistry.supports(chainCode), WalletRespCodeEnum.CHAIN_NOT_SUPPORTED, chainCode);

        validateAddress(chainCode, toAddress);
        Preconditions.needTrue(amount != null && amount.signum() > 0, WalletRespCodeEnum.INVALID_AMOUNT, 6);
        Preconditions.needTrue(amount.compareTo(rule.getMinAmount()) >= 0,
                WalletRespCodeEnum.WITHDRAW_AMOUNT_TOO_SMALL, rule.getMinAmount());
        BigDecimal fee = rule.getFee() == null ? BigDecimal.ZERO : rule.getFee();
        Preconditions.needTrue(amount.compareTo(fee) > 0, WalletRespCodeEnum.WITHDRAW_FEE_INVALID);

        String coinSymbol = StringUtils.hasText(coin) ? coin.trim().toUpperCase(Locale.ROOT) : DEFAULT_COIN;
        WalletWithdrawOrder order = new WalletWithdrawOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(userId);
        order.setChainCode(chainCode.name());
        order.setCoin(coinSymbol);
        order.setAmount(amount);
        order.setFee(fee);
        // 链上实际转出 = 申请额 - 手续费；冻结/结算以申请额为准，手续费差额留热钱包
        order.setReceiveAmount(amount.subtract(fee));
        order.setToAddress(toAddress.trim());
        order.setStatus(WithdrawStatus.REVIEWING.name());
        order.setConfirmations(0);

        txOps.openFrozen(order);
        log.info("提现申请受理 order={} user={} chain={} amount={} fee={}",
                order.getOrderNo(), userId, chainCode, amount, fee);
        return order;
    }

    /**
     * 人工审核通过 → 立即热钱包出款广播。
     * 广播位于 DB 事务之外且不可回滚：先 CAS 把订单移出 REVIEWING（至多一次广播），广播成功置 BROADCASTING，
     * 广播失败置 FAILED 并解冻退款，并把失败原因抛回管理端。
     */
    public WalletWithdrawOrder approve(String orderNo, String reviewer, String remark) throws BizException {
        WalletWithdrawOrder order = txOps.casApprove(orderNo, reviewer, remark);
        try {
            PayoutResult result = payoutService.payout(
                    ChainCode.valueOf(order.getChainCode()), order.getCoin(),
                    order.getToAddress(), order.getReceiveAmount());
            txOps.casBroadcasting(orderNo, result.fromAddress(), result.txHash());
            log.info("提现出款已广播 order={} tx={}", orderNo, result.txHash());
        } catch (BizException e) {
            txOps.casFailedFromApproved(orderNo);
            log.warn("提现出款广播失败已解冻 order={} reason={}", orderNo, e.getMessage());
            throw e;
        }
        return requireByOrderNo(orderNo);
    }

    /** 人工驳回 → 解冻退款。 */
    public WalletWithdrawOrder reject(String orderNo, String reviewer, String remark) throws BizException {
        txOps.casReject(orderNo, reviewer, remark);
        log.info("提现已驳回 order={} reviewer={}", orderNo, reviewer);
        return requireByOrderNo(orderNo);
    }

    /**
     * 对单笔 BROADCASTING 订单查询链上确认数：达标则结算出账(CONFIRMED)，未达标仅刷新确认数。
     * 由确认轮询调度器逐笔调用；幂等，重复调用不会重复结算。
     */
    public void pollConfirm(WalletWithdrawOrder order) throws BizException {
        ChainCode chainCode = ChainCode.valueOf(order.getChainCode());
        WithdrawProperties.ChainRule rule = withdrawProperties.rule(chainCode.name());
        int threshold = rule != null ? rule.getMinConfirmations() : Integer.MAX_VALUE;

        long confirmations = adapterRegistry.get(chainCode).confirmations(order.getTxHash());
        if (confirmations >= threshold) {
            txOps.casConfirmed(order.getOrderNo(), (int) confirmations);
            log.info("提现确认达标已结算 order={} confs={}", order.getOrderNo(), confirmations);
        } else {
            txOps.refreshConfirmations(order.getOrderNo(), (int) confirmations);
        }
    }

    /** 本人提现记录（按时间倒序）。 */
    public List<WalletWithdrawOrder> listUserWithdraws(Long userId) {
        return orderMapper.selectListByQuery(QueryWrapper.create()
                .from(WalletWithdrawOrder.class)
                .eq(WalletWithdrawOrder::getUserId, userId)
                .orderBy(WalletWithdrawOrder::getId, false));
    }

    /** 某状态下的提现订单（审核列表 / 确认轮询用）。 */
    public List<WalletWithdrawOrder> listByStatus(WithdrawStatus status) {
        return orderMapper.selectListByQuery(QueryWrapper.create()
                .from(WalletWithdrawOrder.class)
                .eq(WalletWithdrawOrder::getStatus, status.name())
                .orderBy(WalletWithdrawOrder::getId, true));
    }

    public WalletWithdrawOrder requireByOrderNo(String orderNo) throws BizException {
        WalletWithdrawOrder order = orderMapper.selectOneByQuery(QueryWrapper.create()
                .from(WalletWithdrawOrder.class)
                .eq(WalletWithdrawOrder::getOrderNo, orderNo));
        Preconditions.notNull(order, WalletRespCodeEnum.WITHDRAW_ORDER_NOT_FOUND, orderNo);
        return order;
    }

    private void validateAddress(ChainCode chainCode, String toAddress) throws BizException {
        Preconditions.needTrue(StringUtils.hasText(toAddress), WalletRespCodeEnum.WITHDRAW_ADDRESS_INVALID, "empty");
        String addr = toAddress.trim();
        switch (chainCode) {
            // TRON 主网 base58check 地址：T 开头、定长 34
            case TRON -> Preconditions.needTrue(addr.startsWith("T") && addr.length() == 34,
                    WalletRespCodeEnum.WITHDRAW_ADDRESS_INVALID, addr);
            // EVM 地址：0x 前缀 + 40 位十六进制（ETH/BSC/Polygon 同构）
            case ETH, BSC, POLYGON -> Preconditions.needTrue(addr.matches("^0x[0-9a-fA-F]{40}$"),
                    WalletRespCodeEnum.WITHDRAW_ADDRESS_INVALID, addr);
        }
    }

    private String nextOrderNo() {
        return "W" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}