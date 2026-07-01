package com.mall.wallet.transfer.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 站内转账订单：只承载用户之间的真实资金划转记录。
 * 成功订单必须与 TRANSFER_OUT / TRANSFER_IN 两条钱包流水可对账。
 */
@Data
@Table(value = "wallet_transfer_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class WalletTransferOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long fromUserId;

    private Long toUserId;

    private String coin;

    private BigDecimal amount;

    private String status;

    private String remark;

    private Long finishedAt;
}