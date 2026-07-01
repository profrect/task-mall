package com.mall.wallet.collect;

import com.mall.wallet.collect.enums.CollectStatus;
import com.mall.wallet.collect.mapper.CollectOrderMapper;
import com.mall.wallet.collect.model.entity.CollectOrder;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 归集状态机的原子迁移单元：每个公开方法 = 状态机一条边 = 一次订单 CAS。
 * <p>
 * <strong>与 WithdrawTxOps 的本质差异</strong>：归集是平台内部资金搬运（用户充值地址 → 出款热钱包），
 * 全程<strong>不触发任何账务</strong>（不调 applyLedger、不写 wallet_flow_detail）——用户余额在充值入账时已确定。
 * 故这里只有「订单 CAS」，没有「账务」；这也是把归集独立成 collect 包、而非塞进 withdraw 的根本原因。
 * <p>
 * 为何独立成 bean：归集广播（喂 gas / 归集转账）不可回滚，须置于 DB 事务<strong>之外</strong>；广播前后的状态迁移
 * 各自独立提交。Spring {@code @Transactional} 自调用不走代理，故把原子迁移与编排器（{@link CollectService}）分离，
 * 跨 bean 调用才能让事务代理生效。
 * <p>
 * 并发/崩溃安全（幂等基石③）：每条边用 {@code updateByQuery(... WHERE status=旧态)} 做 CAS，受影响行数=0 即
 * 表示已被并发推进或状态不符——据此幂等短路。归集「同向搬运、重复广播最终无损」（第二次余额必空），
 * 故无需提现那样的「广播中」瞬态隔离。
 */
@Slf4j
@Service
public class CollectTxOps {

    @Resource
    private CollectOrderMapper orderMapper;

    /** 落单：CREATED。创建前的「同地址唯一进行中订单」校验由编排层把关。 */
    @Transactional(rollbackFor = Exception.class)
    public void create(CollectOrder order) {
        orderMapper.insert(order);
    }

    /** CREATED → GAS_FUNDING：回填 gas 交易哈希与广播时间。CAS 落空即已被推进，幂等返回。 */
    @Transactional(rollbackFor = Exception.class)
    public void casGasFunding(String orderNo, String gasTxHash) {
        CollectOrder patch = new CollectOrder();
        patch.setStatus(CollectStatus.GAS_FUNDING.name());
        patch.setGasTxHash(gasTxHash);
        patch.setGasSentAt(System.currentTimeMillis());
        orderMapper.updateByQuery(patch, casQuery(orderNo, CollectStatus.CREATED));
    }

    /**
     * {@code from}(CREATED|GAS_FUNDING) → SWEEPING：回填归集目标 / 实际转出额 / 归集交易哈希与广播时间。
     * CAS 落空即已被推进，幂等返回（归集已广播，无需补偿——重复广播最终无损）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void casSweeping(String orderNo, CollectStatus from, String toAddress, BigDecimal sweptAmount, String sweepTxHash) {
        CollectOrder patch = new CollectOrder();
        patch.setStatus(CollectStatus.SWEEPING.name());
        patch.setToAddress(toAddress);
        patch.setSweptAmount(sweptAmount);
        patch.setSweepTxHash(sweepTxHash);
        patch.setSweptAt(System.currentTimeMillis());
        orderMapper.updateByQuery(patch, casQuery(orderNo, from));
    }

    /** SWEEPING → COMPLETED：归集确认达标，回填确认数与终态时间。CAS 落空即已完成，幂等返回。 */
    @Transactional(rollbackFor = Exception.class)
    public void casCompleted(String orderNo, int confirmations) {
        CollectOrder patch = new CollectOrder();
        patch.setStatus(CollectStatus.COMPLETED.name());
        patch.setConfirmations(confirmations);
        patch.setFinishedAt(System.currentTimeMillis());
        orderMapper.updateByQuery(patch, casQuery(orderNo, CollectStatus.SWEEPING));
    }

    /** 仅刷新归集交易确认数（SWEEPING 未达标时的轻量进度更新，不改状态）。 */
    @Transactional(rollbackFor = Exception.class)
    public void refreshConfirmations(String orderNo, int confirmations) {
        CollectOrder patch = new CollectOrder();
        patch.setConfirmations(confirmations);
        orderMapper.updateByQuery(patch, casQuery(orderNo, CollectStatus.SWEEPING));
    }

    /** {@code from} → FAILED：链上动作失败 / 余额为空，回填失败原因与终态时间。CAS 落空即已被推进，幂等返回。 */
    @Transactional(rollbackFor = Exception.class)
    public void casFailed(String orderNo, CollectStatus from, String remark) {
        CollectOrder patch = new CollectOrder();
        patch.setStatus(CollectStatus.FAILED.name());
        patch.setRemark(remark);
        patch.setFinishedAt(System.currentTimeMillis());
        orderMapper.updateByQuery(patch, casQuery(orderNo, from));
    }

    private QueryWrapper casQuery(String orderNo, CollectStatus expected) {
        return QueryWrapper.create()
                .eq(CollectOrder::getOrderNo, orderNo)
                .eq(CollectOrder::getStatus, expected.name());
    }
}