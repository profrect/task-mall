package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 邀请返佣记录：用户域持有邀请关系与触发业务快照，钱包只负责最终入账流水。 */
@Data
@Table(value = "invite_commission_record", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class InviteCommissionRecord extends BaseEntity<Long> {

    private String recordNo;

    private Long inviterUserId;

    private Long sourceUserId;

    private String sourceOrderNo;

    private String businessType;

    private String currency;

    private BigDecimal sourceAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private String status;

    private String walletFlowNo;

    private String failReason;

    private Long settledAt;
}