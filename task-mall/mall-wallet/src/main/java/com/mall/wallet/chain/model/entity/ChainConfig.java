package com.mall.wallet.chain.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 链运行配置。控制启用哪些链、入账确认门槛。
 * rpcRef/explorerRef 仅为“配置键名”，真实节点 URL 与 API Key 走 yml/env，绝不入库。
 */
@Data
@Table(value = "chain_config", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ChainConfig extends BaseEntity<Long> {

    private String chainCode;

    private Long chainId;

    private String rpcRef;

    private String explorerRef;

    private Integer minConfirmations;

    private Integer enabled;
}