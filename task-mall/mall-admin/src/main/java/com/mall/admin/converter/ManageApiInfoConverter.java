package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageApiInfoDTO;
import com.mall.admin.model.entity.ManageApiInfo;
import com.mall.admin.model.vo.ManageApiInfoVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

/**
 * 系统接口资源信息表(ManageApiInfo)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageApiInfoConverter extends BaseConverter<ManageApiInfo, ManageApiInfoDTO, ManageApiInfoVO> {

}
