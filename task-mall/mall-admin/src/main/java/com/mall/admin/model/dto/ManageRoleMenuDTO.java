package com.mall.admin.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageRoleMenuDTO implements Serializable {
    private static final long serialVersionUID = 578074018961677747L;
    private Long id;
    private String roleCode;
    private String menuKey;
    private Long createTime;
    private Long updateTime;

}

