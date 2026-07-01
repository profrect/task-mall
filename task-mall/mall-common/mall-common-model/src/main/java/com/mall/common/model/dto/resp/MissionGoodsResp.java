package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** 任务中心商品/项目配置响应。 */
@Data
public class MissionGoodsResp {

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

    private Long createTime;

    private Long updateTime;
}