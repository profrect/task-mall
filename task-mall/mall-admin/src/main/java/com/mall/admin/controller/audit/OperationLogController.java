package com.mall.admin.controller.audit;

import com.mall.admin.model.dto.OperationLogQueryDTO;
import com.mall.admin.model.vo.OperationLogVO;
import com.mall.admin.service.audit.OperationLogService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 后台操作日志接口。 */
@RestController
@RequestMapping("/api/admin/operation-log")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<List<OperationLogVO>> list(OperationLogQueryDTO query) throws BizException {
        return Result.ok(operationLogService.list(query));
    }
}