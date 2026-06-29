package com.mall.admin.model.dto;


import com.mall.common.core.valid.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ManageMenuInfoDTO implements Serializable {
    private static final long serialVersionUID = -29060010635854690L;

    @NotNull(message = "【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotBlank(message = "参数【路由名称】不能为空", groups = {ValidGroups.Insert.class})
    @Size(max = 50, message = "参数【路由名称】超过长度限制")
    private String name;

    @NotBlank(message = "参数【路由路径】不能为空", groups = {ValidGroups.Insert.class})
    @Size(max = 50, message = "参数【路由路径】超过长度限制")
    private String path;

    @NotNull(message = "参数【菜单类型】不能为空", groups = {ValidGroups.Insert.class})
    private Integer type;

    @NotBlank(message = "参数【菜单名称key】不能为空", groups = {ValidGroups.Insert.class})
    @Size(max = 100, message = "参数【菜单名称key】超过长度限制")
    private String menuKey;

    @NotBlank(message = "参数【组件路径】不能为空", groups = {ValidGroups.Insert.class})
    @Size(max = 100, message = "参数【组件路径】超过长度限制")
    private String component;

    @NotNull(message = "参数【显示状态】不能为空", groups = {ValidGroups.Insert.class})
    private Integer hideInMenu;

    private String icon;

    @NotNull(message = "参数【父级菜单】不能为空", groups = {ValidGroups.Insert.class})
    private Long pno;

    private Integer menuLevel;

    private Integer order;

    @NotNull(message = "参数【菜单所属标识】不能为空", groups = {ValidGroups.Insert.class})
    private Integer tag;

    @NotNull(message = "【菜单状态】不能为空", groups = {ValidGroups.Insert.class})
    private Integer status;
}

