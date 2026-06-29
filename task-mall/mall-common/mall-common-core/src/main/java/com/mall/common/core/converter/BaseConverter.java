package com.mall.common.core.converter;

import java.util.List;

/**
 * packageName com.nemojoy.platform.common.converter
 *
 * @description mapstruct 对象转换基础方法
 */
public interface BaseConverter<PO, DTO, VO> {

    PO dtoToPo(DTO dto);

    List<PO> dtoToPoList(List<DTO> list);

    VO dtoToVo(DTO dto);

    VO poToVo(PO po);

    List<VO> poToVoList(List<PO> list);

    DTO poToDto(PO po);
}
