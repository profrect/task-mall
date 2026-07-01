package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 钱包账户余额快照（跨服务契约：mall-wallet provider → mall-admin）。 */
@Data
public class WalletAccountResp {

    private Long userId;

    private String currency;

    private BigDecimal totalBalance;

    private BigDecimal availBalance;

    private BigDecimal frozenBalance;
}