package com.mall.mission.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 任务中心商品/项目配置：包含普通商品与投资项目展示口径，不承载资金账本。 */
@Data
@Table(value = "mission_goods", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class MissionGoods extends BaseEntity<Long> {

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