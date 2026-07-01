package com.mall.admin.service.leaderboard.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.leaderboard.LeaderboardAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import com.mall.common.model.dto.req.LeaderboardQueryReq;
import com.mall.common.model.dto.req.UserProfileSummaryBatchReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 排行榜聚合读模型。
 * 数据流：榜单类型 -> 对应事实服务聚合 -> 用户服务补公开资料 -> 统一返回。
 */
@Service
public class LeaderboardAdminServiceImpl implements LeaderboardAdminService {

    private static final String TYPE_EARNING = "EARNING";
    private static final String TYPE_RECHARGE = "RECHARGE";
    private static final String TYPE_TASK = "TASK";
    private static final String PATH_WALLET_RECHARGE = "/api/provider/wallet/flow/leaderboard/recharge";
    private static final String PATH_WALLET_EARNING = "/api/provider/wallet/flow/leaderboard/earning";
    private static final String PATH_MISSION_TASK = "/api/provider/mission/leaderboard/task";
    private static final String PATH_USER_PROFILE_SUMMARIES = "/api/provider/user/profile-summaries";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<LeaderboardItemResp> list(LeaderboardQueryReq req) throws BizException {
        LeaderboardQueryReq q = normalize(req);
        List<LeaderboardItemResp> rows = queryFactRows(q);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        Map<Long, UserProfileSummaryResp> profiles = profileMap(rows);
        return rows.stream()
                .sorted(Comparator.comparing(LeaderboardItemResp::getRankNo, Comparator.nullsLast(Integer::compareTo)))
                .peek(item -> fillProfile(item, profiles.get(item.getUserId())))
                .toList();
    }

    private LeaderboardQueryReq normalize(LeaderboardQueryReq req) throws BizException {
        LeaderboardQueryReq q = req == null ? new LeaderboardQueryReq() : req;
        String type = StringUtils.hasText(q.getType())
                ? q.getType().trim().toUpperCase(Locale.ROOT)
                : TYPE_EARNING;
        if (!List.of(TYPE_EARNING, TYPE_RECHARGE, TYPE_TASK).contains(type)) {
            throw new BizException(CommonRespCode.PARAM_INVALID, new Object[]{"type"});
        }
        LeaderboardQueryReq normalized = new LeaderboardQueryReq();
        normalized.setType(type);
        normalized.setStartTime(q.getStartTime());
        normalized.setEndTime(q.getEndTime());
        normalized.setLimit(safeLimit(q.getLimit()));
        return normalized;
    }

    private List<LeaderboardItemResp> queryFactRows(LeaderboardQueryReq req) throws BizException {
        String path = switch (req.getType()) {
            case TYPE_RECHARGE -> PATH_WALLET_RECHARGE;
            case TYPE_TASK -> PATH_MISSION_TASK;
            default -> PATH_WALLET_EARNING;
        };
        String baseUrl = TYPE_TASK.equals(req.getType())
                ? serviceEndpoints.getMissionBaseUrl()
                : serviceEndpoints.getWalletBaseUrl();
        return apiRestClient.post(baseUrl + path, req, new TypeReference<List<LeaderboardItemResp>>() {});
    }

    private Map<Long, UserProfileSummaryResp> profileMap(List<LeaderboardItemResp> rows) throws BizException {
        List<Long> userIds = rows.stream()
                .map(LeaderboardItemResp::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        UserProfileSummaryBatchReq req = new UserProfileSummaryBatchReq();
        req.setUserIds(userIds);
        List<UserProfileSummaryResp> profiles = apiRestClient.post(
                serviceEndpoints.getUserBaseUrl() + PATH_USER_PROFILE_SUMMARIES,
                req,
                new TypeReference<List<UserProfileSummaryResp>>() {});
        if (profiles == null || profiles.isEmpty()) {
            return Map.of();
        }
        return profiles.stream()
                .filter(p -> p.getUserId() != null)
                .collect(Collectors.toMap(UserProfileSummaryResp::getUserId, Function.identity(), (a, b) -> a));
    }

    private void fillProfile(LeaderboardItemResp item, UserProfileSummaryResp profile) {
        if (profile != null) {
            item.setDisplayName(profile.getDisplayName());
            item.setVipLevel(profile.getVipLevel());
            return;
        }
        if (!StringUtils.hasText(item.getDisplayName()) && item.getUserId() != null) {
            item.setDisplayName("User " + item.getUserId());
        }
    }

    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}