package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageRoleMenuDTO;
import com.mall.admin.model.entity.ManageRoleMenu;
import com.mall.admin.model.vo.ManageRoleMenuVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 角色-菜单关联关系表(ManageRoleMenu)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageRoleMenuConverter extends BaseConverter<ManageRoleMenu, ManageRoleMenuDTO , ManageRoleMenuVO> {

}
