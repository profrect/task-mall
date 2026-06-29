package com.mall.admin.model.dto;


import com.mall.common.core.valid.ValidGroups;
import com.mall.common.model.dto.BasePageDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ManageRoleInfoDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 711919214964082894L;

    @NotNull(message = "参数【id】不能为空", groups = {ValidGroups.Update.class})
    private Long id;

    @NotNull(message = "参数【角色代码】不能为空", groups = {ValidGroups.Insert.class})
    private String roleCode;

    @NotNull(message = "参数【角色名】不能为空", groups = {ValidGroups.Insert.class})
    private String roleName;

    private Integer level;
    private String desc;
}

