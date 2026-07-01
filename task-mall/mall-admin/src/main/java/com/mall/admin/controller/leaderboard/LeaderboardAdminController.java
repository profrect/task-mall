package com.mall.admin.controller.leaderboard;

import com.mall.admin.service.leaderboard.LeaderboardAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.LeaderboardQueryReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 后台排行榜：只读聚合真实流水/任务事实，不生成榜单假数据。 */
@RestController
@RequestMapping("/api/admin/leaderboard")
public class LeaderboardAdminController {

    @Resource
    private LeaderboardAdminService leaderboardAdminService;

    @GetMapping("/list")
    public Result<List<LeaderboardItemResp>> list(LeaderboardQueryReq req) throws BizException {
        return Result.ok(leaderboardAdminService.list(req));
    }
}