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

/** 移动端公开排行榜：只暴露脱敏展示名、VIP 和聚合指标。 */
@RestController
@RequestMapping("/api/open/leaderboard")
public class OpenLeaderboardController {

    @Resource
    private LeaderboardAdminService leaderboardAdminService;

    @GetMapping("/list")
    public Result<List<LeaderboardItemResp>> list(LeaderboardQueryReq req) throws BizException {
        return Result.ok(leaderboardAdminService.list(req));
    }
}