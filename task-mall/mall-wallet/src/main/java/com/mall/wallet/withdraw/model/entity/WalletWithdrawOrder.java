package com.mall.wallet.withdraw.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现订单：用户侧可见的提现记录，同时承载提现状态机。
 * <p>
 * 账务幂等键 = orderNo。提现三阶段（WITHDRAW_FREEZE 申请冻结 / WITHDRAW_SETTLE 确认出账 /
 * WITHDRAW_UNFREEZE 驳回或失败解冻）均以 orderNo 作为 wallet_flow_detail.biz_id，
 * 三者 bizType 不同，互不幂等覆盖（账务内核以 uk_biz 兜底，不会重复冻结/结算/解冻）。
 * <p>
 * 资金语义：amount = 申请总额（冻结/结算均以此为准）；fee = 手续费（平台留存，不拆为用户账变）；
 * receiveAmount = 链上实际转出 = amount - fee（差额 fee 留在热钱包，作为平台收入）。
 */
@Data
@Table(value = "wallet_withdraw_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class WalletWithdrawOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal receiveAmount;

    private String toAddress;

    /** 出款热钱包地址，广播时回填。 */
    private String fromAddress;

    /** 链上交易哈希，广播后回填。 */
    private String txHash;

    private Integer confirmations;

    private String status;

    private String reviewer;

    private String reviewRemark;

    private Long reviewedAt;

    private Long broadcastAt;

    /** 终态时间：确认 / 驳回 / 失败。 */
    private Long finishedAt;
}