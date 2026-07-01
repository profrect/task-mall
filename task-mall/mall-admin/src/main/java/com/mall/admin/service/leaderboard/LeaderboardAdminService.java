package com.mall.admin.service.leaderboard;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.LeaderboardQueryReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;

import java.util.List;

/** 排行榜聚合：wallet/mission 提供事实榜，user 只补公开展示资料。 */
public interface LeaderboardAdminService {

    List<LeaderboardItemResp> list(LeaderboardQueryReq req) throws BizException;
}