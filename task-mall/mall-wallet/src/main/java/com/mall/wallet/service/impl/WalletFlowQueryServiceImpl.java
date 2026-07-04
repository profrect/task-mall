package com.mall.wallet.service.impl;

import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.mapper.WalletFlowDetailMapper;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mall.wallet.service.WalletFlowQueryService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** 账务流水只读查询实现。 */
@Service
public class WalletFlowQueryServiceImpl implements WalletFlowQueryService {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final int MAX_RANK_LIMIT = 100;
    private static final String TYPE_RECHARGE = "RECHARGE";
    private static final String TYPE_EARNING = "EARNING";
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final Set<String> EARNING_BIZ_TYPES = Set.of(
            WalletBizType.TASK_REWARD.name(),
            WalletBizType.TASK_CENTER_REWARD.name(),
            WalletBizType.SHARE_TASK_REWARD.name(),
            WalletBizType.VIDEO_TASK_REWARD.name(),
            WalletBizType.VA_TASK_REWARD.name(),
            WalletBizType.INVITE_COMMISSION.name(),
            WalletBizType.CHECKIN_REWARD.name(),
            WalletBizType.LOTTERY_REWARD.name());

    @Resource
    private WalletFlowDetailMapper flowMapper;

    @Override
    public List<WalletFlowDetail> listUserFlows(Long userId, int limit) {
        return flowMapper.selectListByQuery(QueryWrapper.create().from(WalletFlowDetail.class)
                .eq(WalletFlowDetail::getUserId, userId)
                .orderBy(WalletFlowDetail::getId, false)
                .limit(safeLimit(limit)));
    }

    @Override
    public List<WalletFlowDetail> listForAdmin(Long userId, int limit) {
        QueryWrapper query = QueryWrapper.create().from(WalletFlowDetail.class);
        if (userId != null && userId > 0) {
            query.eq(WalletFlowDetail::getUserId, userId);
        }
        query.orderBy(WalletFlowDetail::getId, false).limit(safeLimit(limit));
        return flowMapper.selectListByQuery(query);
    }

    @Override
    public List<LeaderboardItemResp> rechargeLeaderboard(Long startTime, Long endTime, int limit) {
        AtomicInteger rank = new AtomicInteger(1);
        return flowMapper.leaderboardByBizTypes(List.of(WalletBizType.RECHARGE.name()), startTime, endTime,
                safeRankLimit(limit)).stream()
                .peek(item -> {
                    item.setRankNo(rank.getAndIncrement());
                    fillRankMeta(item, TYPE_RECHARGE, "充值金额");
                })
                .toList();
    }

    @Override
    public List<LeaderboardItemResp> earningLeaderboard(Long startTime, Long endTime, int limit) {
        AtomicInteger rank = new AtomicInteger(1);
        return flowMapper.leaderboardByBizTypes(EARNING_BIZ_TYPES.stream().toList(), startTime, endTime,
                safeRankLimit(limit)).stream()
                .peek(item -> {
                    item.setRankNo(rank.getAndIncrement());
                    fillRankMeta(item, TYPE_EARNING, "收益金额");
                })
                .toList();
    }

    private void fillRankMeta(LeaderboardItemResp item, String type, String label) {
        item.setType(type);
        item.setMetricLabel(label);
        item.setCurrency(DEFAULT_CURRENCY);
    }

    private int safeRankLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_RANK_LIMIT);
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
