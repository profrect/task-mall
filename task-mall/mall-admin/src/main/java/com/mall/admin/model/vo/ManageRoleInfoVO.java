package com.mall.admin.model.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mall.admin.model.entity.ManageApiInfo;
import com.mall.admin.model.entity.ManageMenuInfo;
import com.mybatisflex.annotation.RelationManyToMany;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ManageRoleInfoVO implements Serializable {
    private static final long serialVersionUID = 222897710571299295L;
    private Long id;
    private String roleCode;
    private String roleName;
    private Integer level;
    private String desc;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

    private List<Long> menuIds;
    private List<Long> apiIds;

    @RelationManyToMany(
            joinTable = "manage_role_menu",
            selfField = "roleCode", joinSelfColumn = "role_code",
            targetField = "menuKey", joinTargetColumn = "menu_key"
    )
    @JsonIgnore
    private List<ManageMenuInfo> menuList;

    @RelationManyToMany(
            joinTable = "manage_role_api",
            selfField = "roleCode", joinSelfColumn = "role_code",
            targetField = "apiCode", joinTargetColumn = "api_code"
    )
    @JsonIgnore
    private List<ManageApiInfo> apiList;
}

