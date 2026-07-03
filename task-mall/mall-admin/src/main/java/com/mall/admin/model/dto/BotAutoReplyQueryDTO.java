package com.mall.admin.model.dto;

import lombok.Data;

@Data
public class BotAutoReplyQueryDTO {

    private Long botId;

    private String keyword;

    private Integer status;
}