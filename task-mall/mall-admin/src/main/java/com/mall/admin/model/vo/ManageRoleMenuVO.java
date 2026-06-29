package com.mall.admin.model.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageRoleMenuVO implements Serializable {
    private static final long serialVersionUID = 367865489444532022L;
    private Long id;
    private String roleCode;
    private String menuKey;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

}

