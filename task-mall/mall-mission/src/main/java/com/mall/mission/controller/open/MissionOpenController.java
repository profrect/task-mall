package com.mall.mission.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.mission.model.dto.MissionSubmitDTO;
import com.mall.mission.model.vo.MissionInvestProjectVO;
import com.mall.mission.model.vo.MissionTaskStatsVO;
import com.mall.mission.model.vo.MissionTaskVO;
import com.mall.mission.service.MissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 移动端任务中心接口。本人数据只取当前登录态，不接受前端传 userId。 */
@RestController
@RequestMapping("/api/open/mission")
public class MissionOpenController {

    @Resource
    private MissionService missionService;

    @GetMapping("/stats")
    public Result<MissionTaskStatsVO> stats(@RequestParam(name = "taskType", required = false) String taskType)
            throws BizException {
        return Result.ok(missionService.userStats(AuthUtils.currentUserId(), taskType));
    }

    @GetMapping("/tasks")
    public Result<List<MissionTaskVO>> tasks(
            @RequestParam(name = "status", defaultValue = "available") String status,
            @RequestParam(name = "taskType", required = false) String taskType,
            @RequestParam(name = "limit", required = false) Integer limit) throws BizException {
        return Result.ok(missionService.userTasks(AuthUtils.currentUserId(), status, taskType, limit));
    }

    @PostMapping("/tasks/{taskId}/claim")
    public Result<MissionUserTaskResp> claim(@PathVariable("taskId") Long taskId) throws BizException {
        AuthUtils.ensureNotImpersonated();
        return Result.ok(missionService.claim(AuthUtils.currentUserId(), taskId));
    }

    @PostMapping("/tasks/submit")
    public Result<MissionUserTaskResp> submit(@RequestBody MissionSubmitDTO dto) throws BizException {
        AuthUtils.ensureNotImpersonated();
        return Result.ok(missionService.submit(AuthUtils.currentUserId(), dto));
    }

    @GetMapping("/records")
    public Result<List<MissionUserTaskResp>> records(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "taskType", required = false) String taskType,
            @RequestParam(name = "limit", required = false) Integer limit) throws BizException {
        return Result.ok(missionService.records(AuthUtils.currentUserId(), status, taskType, limit));
    }

    @GetMapping("/invest/projects")
    public Result<List<MissionInvestProjectVO>> investProjects(@RequestParam(name = "limit", required = false) Integer limit)
            throws BizException {
        return Result.ok(missionService.investProjects(limit));
    }
}