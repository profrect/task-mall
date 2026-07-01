package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemParamVO implements Serializable {
    private static final long serialVersionUID = -1993360075325189911L;

    private Long id;
    private String paramKey;
    private String paramValue;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}