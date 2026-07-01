package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 钱包流水视图（跨服务契约：mall-wallet provider → mall-admin；用户侧也复用同字段）。 */
@Data
public class WalletFlowResp {

    private String flowNo;

    private Long userId;

    private String bizType;

    private String bizId;

    private String direction;

    private BigDecimal changeAmt;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String remark;

    private Long createTime;
}