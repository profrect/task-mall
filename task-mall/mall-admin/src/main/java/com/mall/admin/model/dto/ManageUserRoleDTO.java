package com.mall.admin.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageUserRoleDTO implements Serializable {
    private static final long serialVersionUID = 745501884278704355L;
    private Long id;
    private String username;
    private String roleCode;
    private Long createTime;
    private Long updateTime;

}

