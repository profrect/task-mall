package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageUserRoleDTO;
import com.mall.admin.model.entity.ManageUserRole;
import com.mall.admin.model.vo.ManageUserRoleVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 用户-用户角色关联关系表(ManageUserRole)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageUserRoleConverter extends BaseConverter<ManageUserRole, ManageUserRoleDTO, ManageUserRoleVO> {

}
