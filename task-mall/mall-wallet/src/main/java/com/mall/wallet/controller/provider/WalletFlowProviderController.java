package com.mall.wallet.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.LeaderboardQueryReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.common.model.dto.resp.WalletFlowResp;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mall.wallet.service.WalletFlowQueryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包流水 provider 接口（内部，仅供 mall-admin 查询审计流水）。
 * <p>
 * 这里只读展示账务内核 append 的流水事实，不暴露任何余额变更能力。
 */
@RestController
@RequestMapping("/api/provider/wallet/flow")
public class WalletFlowProviderController {

    @Resource
    private WalletFlowQueryService walletFlowQueryService;

    /** 全站账务流水：userId 可选，最新在前，limit 默认 200、最大 1000。 */
    @GetMapping("/list")
    public Result<List<WalletFlowResp>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "200") Integer limit) {
        return Result.ok(walletFlowQueryService.listForAdmin(userId, limit == null ? 200 : limit)
                .stream()
                .map(this::toResp)
                .toList());
    }

    /** 充值排行榜：只按真实充值入账流水聚合。 */
    @PostMapping("/leaderboard/recharge")
    public Result<List<LeaderboardItemResp>> rechargeLeaderboard(@RequestBody LeaderboardQueryReq req) {
        LeaderboardQueryReq q = req == null ? new LeaderboardQueryReq() : req;
        return Result.ok(walletFlowQueryService.rechargeLeaderboard(q.getStartTime(), q.getEndTime(),
                q.getLimit() == null ? 20 : q.getLimit()));
    }

    /** 收益排行榜：只按真实奖励/返佣/抽奖入账流水聚合。 */
    @PostMapping("/leaderboard/earning")
    public Result<List<LeaderboardItemResp>> earningLeaderboard(@RequestBody LeaderboardQueryReq req) {
        LeaderboardQueryReq q = req == null ? new LeaderboardQueryReq() : req;
        return Result.ok(walletFlowQueryService.earningLeaderboard(q.getStartTime(), q.getEndTime(),
                q.getLimit() == null ? 20 : q.getLimit()));
    }

    private WalletFlowResp toResp(WalletFlowDetail f) {
        WalletFlowResp r = new WalletFlowResp();
        r.setFlowNo(f.getFlowNo());
        r.setUserId(f.getUserId());
        r.setBizType(f.getBizType());
        r.setBizId(f.getBizId());
        r.setDirection(f.getDirection());
        r.setChangeAmt(f.getChangeAmt());
        r.setBalanceBefore(f.getBalanceBefore());
        r.setBalanceAfter(f.getBalanceAfter());
        r.setRemark(f.getRemark());
        r.setCreateTime(f.getCreateTime());
        return r;
    }
}