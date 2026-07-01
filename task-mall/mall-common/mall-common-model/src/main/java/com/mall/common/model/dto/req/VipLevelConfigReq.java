package com.mall.common.model.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/** VIP 等级配置写入请求：配置归用户域持久化，钱包只处理升级扣款。 */
@Data
public class VipLevelConfigReq {

    private Long id;

    private Integer level;

    private String levelName;

    private BigDecimal price;

    private BigDecimal rebateRate;

    private Integer dailyTasks;

    private String benefits;

    private Integer sortOrder;

    private Integer status;
}