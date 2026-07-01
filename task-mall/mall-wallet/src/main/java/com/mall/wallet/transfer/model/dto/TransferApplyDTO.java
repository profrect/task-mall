package com.mall.wallet.transfer.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 站内转账申请入参。userId 一律从会话解析，不接受前端传入。 */
@Data
public class TransferApplyDTO {

    private Long toUserId;

    private String coin = "USDT";

    private BigDecimal amount;

    private String remark;
}