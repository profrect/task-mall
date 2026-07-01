package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/** 抽奖奖品配置请求。现金奖只定义金额口径，实际入账由 mall-wallet 结算 provider 完成。 */
@Data
public class PromotionPrizeReq {

    private Long id;

    private String prizeCode;

    private String prizeName;

    private String prizeType;

    private String currency;

    private BigDecimal amount;

    private Integer stockTotal;

    private Integer stockUsed;

    private Integer sortOrder;

    private Integer status;

    private String remark;
}