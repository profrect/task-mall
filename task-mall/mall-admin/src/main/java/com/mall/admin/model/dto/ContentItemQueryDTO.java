package com.mall.admin.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContentItemQueryDTO implements Serializable {
    private static final long serialVersionUID = -8216011478127302295L;

    private String type;
    private String languageCode;
    private Integer status;
}