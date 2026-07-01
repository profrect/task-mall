package com.mall.common.model.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/** VIP 等级配置响应：供管理端与用户端展示真实运营配置。 */
@Data
public class VipLevelConfigResp {

    private Long id;

    private Integer level;

    private String levelName;

    private BigDecimal price;

    private BigDecimal rebateRate;

    private Integer dailyTasks;

    private String benefits;

    private Integer sortOrder;

    private Integer status;

    private Long createTime;

    private Long updateTime;
}