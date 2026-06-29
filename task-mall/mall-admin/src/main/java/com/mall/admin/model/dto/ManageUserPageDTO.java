package com.mall.admin.model.dto;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ManageUserPageDTO extends BasePageDTO {

    private String username;

    private String roleCode;
}
