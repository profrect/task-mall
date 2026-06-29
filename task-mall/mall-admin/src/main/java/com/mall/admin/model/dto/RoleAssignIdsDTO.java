package com.mall.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author xiaoc
 */
@Data
public class RoleAssignIdsDTO {

    @NotBlank(message = "参数【角色代码】不能为空")
    private String roleCode;

    private List<Long> idList;
}
