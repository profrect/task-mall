package com.mall.admin.service.audit;

import com.mall.admin.model.dto.OperationLogQueryDTO;
import com.mall.admin.model.vo.OperationLogVO;
import com.mall.common.core.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/** 后台操作日志服务。 */
public interface OperationLogService {

    List<OperationLogVO> list(OperationLogQueryDTO query) throws BizException;

    void record(HttpServletRequest request, int statusCode, long durationMs);
}