package com.mall.mission.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 投资项目展示口径：不包含认购、派息、到期结算等独立资金状态。 */
@Data
public class MissionInvestProjectVO {

    private Long id;

    private String goodsCode;

    private String title;

    private String description;

    private String currency;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal displayRate;

    private Integer cycleDays;

    private String riskLevel;
}