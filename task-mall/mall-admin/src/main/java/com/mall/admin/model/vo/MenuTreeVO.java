package com.mall.admin.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuTreeVO {
    private Long key;

    private Long pno;

    private String menuKey;

    private Integer type;

    private String name;

    private String path;

    private String component;

    private Integer order;

    private Integer hideInMenu;

    private String icon;

    private List<MenuTreeVO> children;
}
