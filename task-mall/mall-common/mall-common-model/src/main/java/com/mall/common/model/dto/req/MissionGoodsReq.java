package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/** 任务中心商品/项目配置请求；投资项目只作为展示口径，不产生独立资金账本。 */
@Data
public class MissionGoodsReq {

    private Long id;

    private String goodsType;

    private String goodsCode;

    private String title;

    private String description;

    private String currency;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal displayRate;

    private Integer cycleDays;

    private String riskLevel;

    private Integer sortOrder;

    private Integer status;
}