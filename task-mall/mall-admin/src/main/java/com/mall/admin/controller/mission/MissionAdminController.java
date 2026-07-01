package com.mall.admin.controller.mission;

import com.mall.admin.service.mission.MissionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mybatisflex.core.paginate.Page;
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

/** 任务中心后台接口：配置和审核都转发到 mall-mission。 */
@RestController
@RequestMapping("/api/admin/mission")
public class MissionAdminController {

    @Resource
    private MissionAdminService missionAdminService;

    @GetMapping("/goods/list")
    public Result<Page<MissionGoodsResp>> goodsPage(MissionGoodsQueryReq req) throws BizException {
        return Result.ok(missionAdminService.goodsPage(req));
    }

    @PostMapping("/goods")
    public Result<MissionGoodsResp> saveGoods(@RequestBody MissionGoodsReq req) throws BizException {
        return Result.ok(missionAdminService.saveGoods(req));
    }

    @PutMapping("/goods")
    public Result<MissionGoodsResp> updateGoods(@RequestBody MissionGoodsReq req) throws BizException {
        return Result.ok(missionAdminService.saveGoods(req));
    }

    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@PathVariable("id") @NotNull Long id) throws BizException {
        missionAdminService.deleteGoods(id);
        return Result.ok();
    }

    @GetMapping("/tasks/list")
    public Result<Page<MissionTaskResp>> taskPage(MissionTaskQueryReq req) throws BizException {
        return Result.ok(missionAdminService.taskPage(req));
    }

    @PostMapping("/tasks")
    public Result<MissionTaskResp> saveTask(@RequestBody MissionTaskReq req) throws BizException {
        return Result.ok(missionAdminService.saveTask(req));
    }

    @PutMapping("/tasks")
    public Result<MissionTaskResp> updateTask(@RequestBody MissionTaskReq req) throws BizException {
        return Result.ok(missionAdminService.saveTask(req));
    }

    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable("id") @NotNull Long id) throws BizException {
        missionAdminService.deleteTask(id);
        return Result.ok();
    }

    @GetMapping("/records/list")
    public Result<Page<MissionUserTaskResp>> recordPage(MissionUserTaskQueryReq req) throws BizException {
        return Result.ok(missionAdminService.recordPage(req));
    }

    @PostMapping("/records/approve")
    public Result<MissionUserTaskResp> approve(@RequestBody MissionTaskReviewReq req) throws BizException {
        return Result.ok(missionAdminService.approve(req));
    }

    @PostMapping("/records/reject")
    public Result<MissionUserTaskResp> reject(@RequestBody MissionTaskReviewReq req) throws BizException {
        return Result.ok(missionAdminService.reject(req));
    }
}