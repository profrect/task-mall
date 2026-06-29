package com.mall.admin.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuSampleVO {

    private String path;

    private String name;

    private Meta meta;

    private String component;

    private String redirect;

    private List<MenuSampleVO> children;

    @Data
    public static class Meta {

        private String menuKey;

        private String icon;

        private Boolean hideInMenu;

        private Integer order;

        private Boolean keepAlive;
    }
}
