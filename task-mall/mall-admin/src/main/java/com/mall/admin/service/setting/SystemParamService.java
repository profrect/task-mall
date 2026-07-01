package com.mall.admin.service.setting;

import com.mall.admin.model.dto.SystemParamDTO;
import com.mall.admin.model.dto.SystemParamQueryDTO;
import com.mall.admin.model.vo.SystemParamVO;
import com.mall.common.core.exception.BizException;

import java.util.List;

/** 系统参数服务。 */
public interface SystemParamService {

    List<SystemParamVO> list(SystemParamQueryDTO query) throws BizException;

    SystemParamVO save(SystemParamDTO dto) throws BizException;

    SystemParamVO update(SystemParamDTO dto) throws BizException;

    void delete(Long id) throws BizException;
}