package com.mall.admin.converter;

import com.mall.admin.model.dto.ManageMenuInfoDTO;
import com.mall.admin.model.entity.ManageMenuInfo;
import com.mall.admin.model.vo.ManageMenuInfoVO;
import com.mall.common.core.converter.BaseConverter;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 系统菜单资源信息表(ManageMenuInfo)model转换器
 *
 * @author gmxu
 */
@Mapper(componentModel = "spring")
public interface ManageMenuInfoConverter extends BaseConverter<ManageMenuInfo, ManageMenuInfoDTO, ManageMenuInfoVO> {

    @Override
    ManageMenuInfo dtoToPo(ManageMenuInfoDTO manageMenuInfoDTO);

    @Override
    List<ManageMenuInfo> dtoToPoList(List<ManageMenuInfoDTO> list);

    @Override
    ManageMenuInfoVO dtoToVo(ManageMenuInfoDTO manageMenuInfoDTO);

    @Override
    ManageMenuInfoVO poToVo(ManageMenuInfo manageMenuInfo);

    @Override
    List<ManageMenuInfoVO> poToVoList(List<ManageMenuInfo> list);

    @Override
    ManageMenuInfoDTO poToDto(ManageMenuInfo manageMenuInfo);
}
