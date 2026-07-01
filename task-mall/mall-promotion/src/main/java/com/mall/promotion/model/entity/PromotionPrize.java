package com.mall.promotion.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** 奖品配置事实。现金奖配置金额，非现金奖只记录中奖事实，不触发钱包入账。 */
@Data
@Table(value = "promotion_prize", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class PromotionPrize extends BaseEntity<Long> {

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