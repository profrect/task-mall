package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageRoleApiDTO;
import com.mall.admin.model.entity.ManageRoleApi;
import com.mall.admin.model.vo.ManageRoleApiVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 角色-接口关联关系表(ManageRoleApi)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageRoleApiConverter extends BaseConverter<ManageRoleApi, ManageRoleApiDTO, ManageRoleApiVO> {

}
