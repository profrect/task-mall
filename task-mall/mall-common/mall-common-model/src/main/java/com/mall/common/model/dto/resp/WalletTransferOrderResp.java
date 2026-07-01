package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 站内转账订单视图（跨服务契约：mall-wallet provider → mall-admin；用户侧也复用同字段）。 */
@Data
public class WalletTransferOrderResp {

    private String orderNo;

    private Long fromUserId;

    private Long toUserId;

    private String coin;

    private BigDecimal amount;

    private String status;

    private String remark;

    private Long finishedAt;

    private Long createTime;
}