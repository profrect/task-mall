package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现订单视图（跨服务契约：mall-wallet provider → mall-admin）。
 * <p>
 * 较用户侧 WithdrawOrderVO 额外暴露 userId / reviewer / fromAddress，供管理端审核与稽核；
 * 金额三元组语义：amount=申请总额、fee=手续费、receiveAmount=链上实际转出(=amount-fee)。
 */
@Data
public class WithdrawOrderResp {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal receiveAmount;

    private String toAddress;

    private String fromAddress;

    private String txHash;

    private Integer confirmations;

    private String status;

    private String reviewer;

    private String reviewRemark;

    private Long reviewedAt;

    private Long broadcastAt;

    private Long finishedAt;

    private Long createTime;
}