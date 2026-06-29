package com.mall.admin.model.vo;


import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;

@Data
public class ManageUserRoleVO implements Serializable {
    private static final long serialVersionUID = -78745047262832805L;
    private Long id;
    @Column(value = "user_name")
    private String username;
    private String roleCode;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

}

