package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageUserInfoDTO;

import com.mall.admin.model.entity.ManageUserInfo;
import com.mall.admin.model.vo.ManageUserInfoVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 用户信息表(ManageUserInfo)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageUserInfoConverter extends BaseConverter<ManageUserInfo, ManageUserInfoDTO, ManageUserInfoVO> {

}
