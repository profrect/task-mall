package com.mall.admin.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageRoleApiDTO implements Serializable {
    private static final long serialVersionUID = -17476498151215312L;
    private Long id;
    private String roleCode;
    private String apiCode;
    private Long createTime;
    private Long updateTime;

}

