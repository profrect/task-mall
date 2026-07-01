package com.mall.wallet.withdraw.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现申请入参（用户侧）。
 * <p>
 * 不含 userId——一律由 sa-token 从会话解析，绝不接受前端传入，从根上杜绝越权提现他人余额。
 */
@Data
public class WithdrawApplyDTO {

    /** 目标链，默认 TRON。 */
    private String chain = "TRON";

    /** 币种，默认 USDT。 */
    private String coin = "USDT";

    /** 收款地址。 */
    private String toAddress;

    /** 提现金额（币种单位，含手续费）。 */
    private BigDecimal amount;
}