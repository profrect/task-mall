package com.mall.admin.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemParamQueryDTO implements Serializable {
    private static final long serialVersionUID = 1129453334549316556L;

    private String keyword;
    private Integer status;
}