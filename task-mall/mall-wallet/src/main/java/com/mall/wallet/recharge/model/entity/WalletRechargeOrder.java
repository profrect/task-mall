package com.mall.wallet.recharge.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值订单：用户侧可见的充值记录（business 层），与 chain_deposit_event（infra 层观测事实）分层。
 * <p>
 * 幂等键 uk_tx(chain_code, tx_hash, log_index)：一笔链上转账只生成一条充值订单。
 * 状态机：CONFIRMING（已观测，确认中） -> CREDITED（确认达标且账务已入账）。
 * 真正的资金入账幂等由 wallet_flow_detail 的 uk_biz(biz_type,biz_id) 兜底，本表状态仅作用户展示与去重。
 */
@Data
@Table(value = "wallet_recharge_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class WalletRechargeOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    private BigDecimal amount;

    private String fromAddress;

    private String toAddress;

    private String txHash;

    private Integer logIndex;

    private Integer confirmations;

    private String status;

    private Long creditedAt;
}