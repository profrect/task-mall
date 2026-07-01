package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 归集订单视图（跨服务契约：mall-wallet provider → mall-admin）。
 * <p>
 * 归集 = 把用户充值地址上的 USDT 搬到出款热钱包，是平台内部资金搬运，<strong>不涉及用户账务</strong>。
 * 金额语义：amount=创建时余额快照；sweptAmount=实际归集转出额（广播时刻实时全余额，可能 != amount）。
 */
@Data
public class CollectOrderResp {

    private String orderNo;

    private Long userId;

    private String chainCode;

    private String coin;

    private String fromAddress;

    private String toAddress;

    private BigDecimal amount;

    private BigDecimal sweptAmount;

    private String gasTxHash;

    private String sweepTxHash;

    private Integer confirmations;

    private String status;

    private String remark;

    private Long gasSentAt;

    private Long sweptAt;

    private Long finishedAt;

    private Long createTime;
}