package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/** VIP 等级运营配置：用户端展示与升级价格的唯一数据源。 */
@Data
@Table(value = "vip_level_config", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class VipLevelConfig extends BaseEntity<Long> {

    private Integer level;

    private String levelName;

    private BigDecimal price;

    private BigDecimal rebateRate;

    private Integer dailyTasks;

    private String benefits;

    private Integer sortOrder;

    private Integer status;
}