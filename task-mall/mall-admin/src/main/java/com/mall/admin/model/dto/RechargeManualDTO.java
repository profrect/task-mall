package com.mall.admin.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端人工充值补单入参。
 *
 * 操作人由服务端登录态注入；前端只提交补单事实和审核备注。
 */
@Data
public class RechargeManualDTO {

    /** 目标会员 UID。 */
    private Long userId;

    /** 入账金额。 */
    private BigDecimal amount;

    /** 币种，当前默认 USDT。 */
    private String coin;

    /** 线下凭证号 / 支付流水号 / 客服记录号。 */
    private String referenceNo;

    /** 补单原因 / 审核备注。 */
    private String remark;
}