package com.mall.wallet.chain.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 币种配置。每(链,符号)的合约地址与链上精度。
 * decimals 用于把链上原始整数金额归一化到账务标度。
 */
@Data
@Table(value = "coin_config", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class CoinConfig extends BaseEntity<Long> {

    private String chainCode;

    private String symbol;

    private String contractAddress;

    private Integer decimals;

    private Integer enabled;
}