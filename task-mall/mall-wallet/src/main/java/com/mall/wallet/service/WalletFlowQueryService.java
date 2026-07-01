package com.mall.wallet.service;

import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.wallet.model.entity.WalletFlowDetail;

import java.util.List;

/**
 * 账务流水查询服务：只读视图，服务于用户流水页与管理端审计页。
 * 余额变更仍只允许经 WalletAccountService.applyLedger 完成。
 */
public interface WalletFlowQueryService {

    /** 用户侧本人流水，最新在前，限量返回。 */
    List<WalletFlowDetail> listUserFlows(Long userId, int limit);

    /** 管理端全站流水，userId 可选，最新在前，限量返回。 */
    List<WalletFlowDetail> listForAdmin(Long userId, int limit);

    /** 充值排行榜：按真实充值入账流水聚合。 */
    List<LeaderboardItemResp> rechargeLeaderboard(Long startTime, Long endTime, int limit);

    /** 收益排行榜：按真实奖励/返佣入账流水聚合。 */
    List<LeaderboardItemResp> earningLeaderboard(Long startTime, Long endTime, int limit);
}