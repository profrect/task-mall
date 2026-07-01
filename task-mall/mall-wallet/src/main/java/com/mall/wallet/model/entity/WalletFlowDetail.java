package com.mall.wallet.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包流水明细。每次余额变动追加一条，作为账变审计与对账依据。
 * 幂等底线：uk_biz(bizType, bizId) 保证同一业务只入账一次。
 * changeAmt 恒为正，资金增减由 direction 表达；balanceBefore/After 为可用余额快照。
 */
@Data
@Table(value = "wallet_flow_detail", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class WalletFlowDetail extends BaseEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String flowNo;

    private Long userId;

    private Long walletId;

    private String bizType;

    private String bizId;

    private String direction;

    private BigDecimal changeAmt;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String remark;
}