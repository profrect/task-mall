package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值订单视图（跨服务契约：mall-wallet provider → mall-admin）。
 * <p>
 * 较用户侧 RechargeOrderVO 额外暴露 userId，供管理端按用户稽核充值到账情况。
 * 充值是链上自动入账（扫块达确认数即记账），管理端为只读监控，无审批动作。
 * 状态机：CONFIRMING（已观测确认中） → CREDITED（确认达标且账务已入账）。
 */
@Data
public class RechargeOrderResp {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    private BigDecimal amount;

    private String fromAddress;

    private String toAddress;

    private String txHash;

    private Integer confirmations;

    private String status;

    private Long creditedAt;

    private Long createTime;
}