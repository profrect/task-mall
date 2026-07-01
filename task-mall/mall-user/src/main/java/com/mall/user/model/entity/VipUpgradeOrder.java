package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** VIP 升级订单：每次成功升级都必须能对账到一条 VIP_UPGRADE 钱包扣款流水。 */
@Data
@Table(value = "vip_upgrade_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class VipUpgradeOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long userId;

    private Integer fromLevel;

    private Integer toLevel;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String walletFlowNo;

    private Long finishedAt;

    private String remark;
}