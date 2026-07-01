package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContentItemVO implements Serializable {
    private static final long serialVersionUID = -7013818794763249879L;

    private Long id;
    private String type;
    private String languageCode;
    private String title;
    private String summary;
    private String content;
    private Integer sortOrder;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}