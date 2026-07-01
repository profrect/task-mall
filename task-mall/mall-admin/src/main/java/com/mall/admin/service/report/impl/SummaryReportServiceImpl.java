package com.mall.admin.service.report.impl;

import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.report.SummaryReportService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.AdminSummaryReportResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.common.model.dto.resp.WalletStatsResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 综合报表聚合：user 负责会员口径，wallet 负责资金口径，admin 只做一次快照拼装。
 * 暂不接 mission / promotion / invest：这些模块尚未形成真实数据源，页面不展示虚假收益或任务数据。
 */
@Service
public class SummaryReportServiceImpl implements SummaryReportService {

    private static final String USER_STATS_PATH = "/api/provider/user/stats";
    private static final String WALLET_STATS_PATH = "/api/provider/wallet/account/stats";
    private static final String DEFAULT_CURRENCY = "USDT";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public AdminSummaryReportResp summary() throws BizException {
        long todayStartAt = todayStartAt();
        UserStatsResp userStats = apiRestClient.get(
                userUrl(USER_STATS_PATH) + "?todayStartAt=" + todayStartAt, UserStatsResp.class);
        WalletStatsResp walletStats = apiRestClient.get(
                walletUrl(WALLET_STATS_PATH) + "?currency=" + DEFAULT_CURRENCY + "&todayStartAt=" + todayStartAt,
                WalletStatsResp.class);

        AdminSummaryReportResp resp = new AdminSummaryReportResp();
        resp.setTotalUsers(nz(userStats == null ? null : userStats.getTotalUsers()));
        resp.setTodayNewUsers(nz(userStats == null ? null : userStats.getTodayNewUsers()));

        resp.setTotalBalance(nz(walletStats == null ? null : walletStats.getTotalBalance()));
        resp.setAvailableBalance(nz(walletStats == null ? null : walletStats.getAvailableBalance()));
        resp.setFrozenBalance(nz(walletStats == null ? null : walletStats.getFrozenBalance()));
        resp.setWalletAccounts(nz(walletStats == null ? null : walletStats.getWalletAccounts()));

        resp.setRechargeOrders(nz(walletStats == null ? null : walletStats.getRechargeOrders()));
        resp.setRechargeAmount(nz(walletStats == null ? null : walletStats.getRechargeAmount()));
        resp.setTodayRechargeOrders(nz(walletStats == null ? null : walletStats.getTodayRechargeOrders()));
        resp.setTodayRechargeAmount(nz(walletStats == null ? null : walletStats.getTodayRechargeAmount()));

        resp.setWithdrawOrders(nz(walletStats == null ? null : walletStats.getWithdrawOrders()));
        resp.setWithdrawAmount(nz(walletStats == null ? null : walletStats.getWithdrawAmount()));
        resp.setReviewingWithdrawOrders(nz(walletStats == null ? null : walletStats.getReviewingWithdrawOrders()));
        resp.setTodayWithdrawOrders(nz(walletStats == null ? null : walletStats.getTodayWithdrawOrders()));
        resp.setTodayWithdrawAmount(nz(walletStats == null ? null : walletStats.getTodayWithdrawAmount()));

        resp.setCollectOrders(nz(walletStats == null ? null : walletStats.getCollectOrders()));
        resp.setCollectedAmount(nz(walletStats == null ? null : walletStats.getCollectedAmount()));
        resp.setActiveCollectOrders(nz(walletStats == null ? null : walletStats.getActiveCollectOrders()));
        resp.setTodayCollectOrders(nz(walletStats == null ? null : walletStats.getTodayCollectOrders()));
        resp.setTodayCollectedAmount(nz(walletStats == null ? null : walletStats.getTodayCollectedAmount()));
        resp.setGeneratedAt(System.currentTimeMillis());
        return resp;
    }

    private long todayStartAt() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private Long nz(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String userUrl(String path) {
        return serviceEndpoints.getUserBaseUrl() + path;
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}