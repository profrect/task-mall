package com.mall.mission.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.LeaderboardQueryReq;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.mission.service.MissionService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理端 provider：后台只聚合，不直连任务库。 */
@RestController
@RequestMapping("/api/provider/mission")
public class MissionProviderController {

    @Resource
    private MissionService missionService;

    @PostMapping("/goods/page")
    public Result<Page<MissionGoodsResp>> goodsPage(@RequestBody MissionGoodsQueryReq req) throws BizException {
        return Result.ok(missionService.goodsPage(req));
    }

    @PostMapping("/goods/save")
    public Result<MissionGoodsResp> saveGoods(@RequestBody MissionGoodsReq req) throws BizException {
        return Result.ok(missionService.saveGoods(req));
    }

    @PostMapping("/goods/delete")
    public Result<Void> deleteGoods(@RequestBody IdReq req) throws BizException {
        missionService.deleteGoods(req.getId());
        return Result.ok();
    }

    @PostMapping("/tasks/page")
    public Result<Page<MissionTaskResp>> taskPage(@RequestBody MissionTaskQueryReq req) throws BizException {
        return Result.ok(missionService.taskPage(req));
    }

    @PostMapping("/tasks/save")
    public Result<MissionTaskResp> saveTask(@RequestBody MissionTaskReq req) throws BizException {
        return Result.ok(missionService.saveTask(req));
    }

    @PostMapping("/tasks/delete")
    public Result<Void> deleteTask(@RequestBody IdReq req) throws BizException {
        missionService.deleteTask(req.getId());
        return Result.ok();
    }

    @PostMapping("/records/page")
    public Result<Page<MissionUserTaskResp>> recordPage(@RequestBody MissionUserTaskQueryReq req) throws BizException {
        return Result.ok(missionService.userTaskPage(req));
    }

    @PostMapping("/leaderboard/task")
    public Result<List<LeaderboardItemResp>> taskLeaderboard(@RequestBody LeaderboardQueryReq req) throws BizException {
        LeaderboardQueryReq q = req == null ? new LeaderboardQueryReq() : req;
        return Result.ok(missionService.taskLeaderboard(q.getStartTime(), q.getEndTime(), q.getLimit()));
    }

    @PostMapping("/records/approve")
    public Result<MissionUserTaskResp> approve(@RequestBody MissionTaskReviewReq req) throws BizException {
        return Result.ok(missionService.approve(req));
    }

    @PostMapping("/records/reject")
    public Result<MissionUserTaskResp> reject(@RequestBody MissionTaskReviewReq req) throws BizException {
        return Result.ok(missionService.reject(req));
    }
}