package com.mall.wallet.chain.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 链上充值事件。扫块写入的“链上观测事实”（基础设施层），与用户侧充值订单分层。
 * 幂等键 = chainCode + txHash + logIndex；amountRaw 为链上原始整数金额（未归一化）。
 * status: SEEN -> CONFIRMED -> CREDITED；IGNORED 表示非充值地址/无关转账。
 */
@Data
@Table(value = "chain_deposit_event", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ChainDepositEvent extends BaseEntity<Long> {

    private String chainCode;

    private String contractAddress;

    private String txHash;

    private Integer logIndex;

    private String fromAddress;

    private String toAddress;

    private String amountRaw;

    private Long blockHeight;

    private Integer confirmations;

    private String status;
}