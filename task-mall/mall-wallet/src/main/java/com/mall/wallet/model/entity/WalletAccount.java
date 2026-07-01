package com.mall.wallet.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包账户主表。账务内核 applyLedger 是唯一可改其余额的入口。
 * 不变式：totalBalance = availBalance + frozenBalance，三者恒 >= 0。
 */
@Data
@Table(value = "wallet_account", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class WalletAccount extends BaseEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String currency;

    private BigDecimal totalBalance;

    private BigDecimal availBalance;

    private BigDecimal frozenBalance;
}