package com.mall.wallet.payment.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付订单审计表：记录支付通道或外部支付观察事实。
 * 余额变化仍只能由对应业务状态机调用钱包账务内核完成，本表不作为入账凭证。
 */
@Data
@Table(value = "payment_order", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PaymentOrder extends BaseEntity<Long> {

    private String orderNo;

    private Long userId;

    private String businessType;

    private String businessOrderNo;

    private String channelCode;

    private String channelOrderNo;

    private String currency;

    private BigDecimal amount;

    private String status;

    private String payAddress;

    private String payerAddress;

    private String txHash;

    private String auditRemark;

    private Long paidAt;

    private Long expiredAt;
}