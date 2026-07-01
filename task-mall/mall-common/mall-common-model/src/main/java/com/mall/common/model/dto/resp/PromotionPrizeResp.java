package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 抽奖奖品配置响应。 */
@Data
public class PromotionPrizeResp {

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

    private Long createTime;

    private Long updateTime;
}