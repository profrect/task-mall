package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BotConfigVO implements Serializable {
    private static final long serialVersionUID = 2793935963895024763L;

    private Long id;
    private String botName;
    private String maskedToken;
    private String webhookUrl;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}