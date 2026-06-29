package com.mall.admin.model.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageRoleApiVO implements Serializable {
    private static final long serialVersionUID = -32049049578263852L;
    private Long id;
    private String roleCode;
    private String apiCode;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

}

