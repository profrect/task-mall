package com.mall.admin.model.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mall.admin.model.entity.ManageRoleInfo;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.RelationManyToMany;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ManageUserInfoVO implements Serializable {
    private static final long serialVersionUID = 977269099134023032L;
    private Long id;
    @Column(value = "user_name")
    private String username;
    private String realName;
    private String password;
    private String email;
    private Integer level;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

    private String roles;

    @RelationManyToMany(
            joinTable = "manage_user_role",
            selfField = "username", joinSelfColumn = "user_name",
            targetField = "roleCode", joinTargetColumn = "role_code"
    )
    @JsonIgnore
    private List<ManageRoleInfo> roleList;
}

