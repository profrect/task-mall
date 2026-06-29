package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageRoleInfoDTO;
import com.mall.admin.model.entity.ManageRoleInfo;
import com.mall.admin.model.vo.ManageRoleInfoVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 角色信息表(ManageRoleInfo)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageRoleInfoConverter extends BaseConverter<ManageRoleInfo, ManageRoleInfoDTO, ManageRoleInfoVO> {

}
