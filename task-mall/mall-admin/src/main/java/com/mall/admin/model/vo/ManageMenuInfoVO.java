package com.mall.admin.model.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageMenuInfoVO implements Serializable {
    private static final long serialVersionUID = -53908520245593552L;
    private Long id;
    private String menuKey;
    private String name;
    private String path;
    private Integer type;
    private String component;
    private Integer hideInMenu;
    private String icon;
    private Long pno;
    private Integer menuLevel;
    private Integer order;
    private Integer tag;
    private Integer status;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

}

