package com.mall.admin.service.content;

import com.mall.admin.model.dto.ContentItemDTO;
import com.mall.admin.model.dto.ContentItemQueryDTO;
import com.mall.admin.model.vo.ContentItemVO;
import com.mall.common.core.exception.BizException;

import java.util.List;

public interface ContentItemService {

    List<ContentItemVO> list(ContentItemQueryDTO query) throws BizException;

    ContentItemVO detail(Long id) throws BizException;

    ContentItemVO save(ContentItemDTO dto) throws BizException;

    ContentItemVO update(ContentItemDTO dto) throws BizException;

    void delete(Long id) throws BizException;
}