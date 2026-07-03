package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BotAutoReplyVO implements Serializable {
    private static final long serialVersionUID = 2987212923516827218L;

    private Long id;
    private Long botId;
    private String keyword;
    private String replyContent;
    private Integer matchType;
    private Integer sortOrder;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}