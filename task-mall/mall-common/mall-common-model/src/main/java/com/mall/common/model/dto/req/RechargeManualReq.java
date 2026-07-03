package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 人工充值补单指令（mall-admin → mall-wallet provider）。
 *
 * operator 由 mall-admin 从登录态注入，前端只允许提交会员、金额、凭证和备注。
 */
@Data
public class RechargeManualReq {

    /** 目标会员 UID。 */
    private Long userId;

    /** 入账金额，USDT 精度不超过 6 位。 */
    private BigDecimal amount;

    /** 币种，当前默认 USDT。 */
    private String coin;

    /** 人工凭证号，可填线下订单号、支付流水号或客服记录号。 */
    private String referenceNo;

    /** 补单原因 / 审核备注。 */
    private String remark;

    /** 操作人，由 mall-admin 服务端注入。 */
    private String operator;
}