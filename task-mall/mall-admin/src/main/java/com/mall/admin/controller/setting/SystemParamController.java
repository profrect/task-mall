package com.mall.admin.controller.setting;

import com.mall.admin.model.dto.SystemParamDTO;
import com.mall.admin.model.dto.SystemParamQueryDTO;
import com.mall.admin.model.vo.SystemParamVO;
import com.mall.admin.service.setting.SystemParamService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.core.valid.ValidGroups;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 系统参数接口。 */
@RestController
@RequestMapping("/api/admin/system-param")
public class SystemParamController {

    @Resource
    private SystemParamService systemParamService;

    @GetMapping("/list")
    public Result<List<SystemParamVO>> list(SystemParamQueryDTO query) throws BizException {
        return Result.ok(systemParamService.list(query));
    }

    @PostMapping
    public Result<SystemParamVO> save(
            @RequestBody @Validated({ValidGroups.Insert.class}) SystemParamDTO dto) throws BizException {
        return Result.ok(systemParamService.save(dto));
    }

    @PutMapping
    public Result<SystemParamVO> update(
            @RequestBody @Validated({ValidGroups.Update.class}) SystemParamDTO dto) throws BizException {
        return Result.ok(systemParamService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @NotNull Long id) throws BizException {
        systemParamService.delete(id);
        return Result.ok();
    }
}