package com.mall.admin.controller.vip;

import com.mall.admin.service.vip.VipAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** VIP 配置管理接口。 */
@RestController
@RequestMapping("/api/admin/vip")
public class VipAdminController {

    @Resource
    private VipAdminService vipAdminService;

    @GetMapping("/config/list")
    public Result<List<VipLevelConfigResp>> list(Integer status) throws BizException {
        return Result.ok(vipAdminService.list(status));
    }

    @PostMapping("/config")
    public Result<VipLevelConfigResp> save(@RequestBody VipLevelConfigReq req) throws BizException {
        return Result.ok(vipAdminService.save(req));
    }

    @PutMapping("/config")
    public Result<VipLevelConfigResp> update(@RequestBody VipLevelConfigReq req) throws BizException {
        return Result.ok(vipAdminService.save(req));
    }

    @DeleteMapping("/config/{id}")
    public Result<Void> delete(@PathVariable("id") @NotNull Long id) throws BizException {
        vipAdminService.delete(id);
        return Result.ok();
    }
}