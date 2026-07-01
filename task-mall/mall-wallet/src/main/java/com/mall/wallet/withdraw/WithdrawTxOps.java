package com.mall.wallet.withdraw;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.service.WalletAccountService;
import com.mall.wallet.withdraw.enums.WithdrawStatus;
import com.mall.wallet.withdraw.mapper.WalletWithdrawOrderMapper;
import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提现状态机的原子迁移单元：每个公开方法 = 状态机一条边 = 一次「订单 CAS + 账务」原子事务。
 * <p>
 * 为何独立成 bean：提现 approve 的链上广播不可回滚，必须置于 DB 事务<strong>之外</strong>；广播前后的状态迁移
 * 需各自独立提交。Spring {@code @Transactional} 自调用不走代理，故把这些原子迁移与编排器（WithdrawService）分离，
 * 跨 bean 调用才能让事务代理生效。
 * <p>
 * 并发/崩溃安全：每条边用 {@code updateByQuery(... WHERE status=旧态)} 做 CAS，受影响行数=0 即表示
 * 已被并发处理或状态不符——据此幂等短路或报状态非法，杜绝重复冻结/结算/解冻与重复广播。
 * 账务侧再以 (bizType, orderNo) 幂等兜底（FREEZE/SETTLE/UNFREEZE 三 bizType 互不覆盖）。
 */
@Slf4j
@Service
public class WithdrawTxOps {

    @Resource
    private WalletWithdrawOrderMapper orderMapper;

    @Resource
    private WalletAccountService walletAccountService;

    /** apply：落单(REVIEWING) + 冻结 amount。余额不足则整体回滚，不留订单。 */
    @Transactional(rollbackFor = Exception.class)
    public void openFrozen(WalletWithdrawOrder order) throws BizException {
        walletAccountService.getOrCreate(order.getUserId(), order.getCoin());
        orderMapper.insert(order);
        walletAccountService.applyLedger(LedgerEvent.of(
                order.getUserId(), WalletBizType.WITHDRAW_FREEZE, order.getOrderNo(), order.getAmount()));
    }

    /** approve 第一步：REVIEWING→APPROVED 原子 CAS（广播前移出可审核态，杜绝并发双广播）。返回 APPROVED 后的订单供广播。 */
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawOrder casApprove(String orderNo, String reviewer, String remark) throws BizException {
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setStatus(WithdrawStatus.APPROVED.name());
        patch.setReviewer(reviewer);
        patch.setReviewRemark(remark);
        patch.setReviewedAt(System.currentTimeMillis());
        int n = orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.REVIEWING));
        WalletWithdrawOrder order = requireByOrderNo(orderNo);
        Preconditions.needTrue(n > 0, WalletRespCodeEnum.WITHDRAW_STATUS_ILLEGAL, order.getStatus());
        return order;
    }

    /** approve 成功：APPROVED→BROADCASTING + 回填 txHash/fromAddress，原子 CAS。 */
    @Transactional(rollbackFor = Exception.class)
    public void casBroadcasting(String orderNo, String fromAddress, String txHash) throws BizException {
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setStatus(WithdrawStatus.BROADCASTING.name());
        patch.setFromAddress(fromAddress);
        patch.setTxHash(txHash);
        patch.setBroadcastAt(System.currentTimeMillis());
        int n = orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.APPROVED));
        Preconditions.needTrue(n > 0, WalletRespCodeEnum.WITHDRAW_STATUS_ILLEGAL, WithdrawStatus.APPROVED.name());
    }

    /** approve 广播失败：APPROVED→FAILED + 解冻退款，原子。CAS 落空即已被并发处理，幂等返回。 */
    @Transactional(rollbackFor = Exception.class)
    public void casFailedFromApproved(String orderNo) throws BizException {
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setStatus(WithdrawStatus.FAILED.name());
        patch.setFinishedAt(System.currentTimeMillis());
        int n = orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.APPROVED));
        if (n == 0) {
            return;
        }
        WalletWithdrawOrder order = requireByOrderNo(orderNo);
        walletAccountService.applyLedger(LedgerEvent.of(
                order.getUserId(), WalletBizType.WITHDRAW_UNFREEZE, orderNo, order.getAmount()));
    }

    /** reject：REVIEWING→REJECTED + 解冻退款，原子。 */
    @Transactional(rollbackFor = Exception.class)
    public void casReject(String orderNo, String reviewer, String remark) throws BizException {
        long now = System.currentTimeMillis();
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setStatus(WithdrawStatus.REJECTED.name());
        patch.setReviewer(reviewer);
        patch.setReviewRemark(remark);
        patch.setReviewedAt(now);
        patch.setFinishedAt(now);
        int n = orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.REVIEWING));
        WalletWithdrawOrder order = requireByOrderNo(orderNo);
        Preconditions.needTrue(n > 0, WalletRespCodeEnum.WITHDRAW_STATUS_ILLEGAL, order.getStatus());
        walletAccountService.applyLedger(LedgerEvent.of(
                order.getUserId(), WalletBizType.WITHDRAW_UNFREEZE, orderNo, order.getAmount()));
    }

    /** confirm：BROADCASTING→CONFIRMED + 结算出账，原子。CAS 落空即已结算，幂等跳过。 */
    @Transactional(rollbackFor = Exception.class)
    public void casConfirmed(String orderNo, int confirmations) throws BizException {
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setStatus(WithdrawStatus.CONFIRMED.name());
        patch.setConfirmations(confirmations);
        patch.setFinishedAt(System.currentTimeMillis());
        int n = orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.BROADCASTING));
        if (n == 0) {
            return;
        }
        WalletWithdrawOrder order = requireByOrderNo(orderNo);
        walletAccountService.applyLedger(LedgerEvent.of(
                order.getUserId(), WalletBizType.WITHDRAW_SETTLE, orderNo, order.getAmount()));
    }

    /** 仅刷新确认数（BROADCASTING 未达标时的轻量进度更新，不改状态、不动账）。 */
    @Transactional(rollbackFor = Exception.class)
    public void refreshConfirmations(String orderNo, int confirmations) {
        WalletWithdrawOrder patch = new WalletWithdrawOrder();
        patch.setConfirmations(confirmations);
        orderMapper.updateByQuery(patch, casQuery(orderNo, WithdrawStatus.BROADCASTING));
    }

    private QueryWrapper casQuery(String orderNo, WithdrawStatus expected) {
        return QueryWrapper.create()
                .eq(WalletWithdrawOrder::getOrderNo, orderNo)
                .eq(WalletWithdrawOrder::getStatus, expected.name());
    }

    private WalletWithdrawOrder requireByOrderNo(String orderNo) throws BizException {
        WalletWithdrawOrder order = orderMapper.selectOneByQuery(QueryWrapper.create()
                .from(WalletWithdrawOrder.class)
                .eq(WalletWithdrawOrder::getOrderNo, orderNo));
        Preconditions.notNull(order, WalletRespCodeEnum.WITHDRAW_ORDER_NOT_FOUND, orderNo);
        return order;
    }
}