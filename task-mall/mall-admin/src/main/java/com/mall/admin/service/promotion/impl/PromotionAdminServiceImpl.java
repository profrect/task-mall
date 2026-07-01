package com.mall.admin.service.promotion.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.promotion.PromotionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.PromotionLotteryActivityQueryReq;
import com.mall.common.model.dto.req.PromotionLotteryActivityReq;
import com.mall.common.model.dto.req.PromotionLotteryPrizeReq;
import com.mall.common.model.dto.req.PromotionLotteryRecordQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeReq;
import com.mall.common.model.dto.resp.PromotionLotteryActivityResp;
import com.mall.common.model.dto.resp.PromotionLotteryPrizeResp;
import com.mall.common.model.dto.resp.PromotionLotteryRecordResp;
import com.mall.common.model.dto.resp.PromotionPrizeResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理端促销聚合：抽奖事实由 mall-promotion 持有，后台不直连促销库。 */
@Service
public class PromotionAdminServiceImpl implements PromotionAdminService {

    private static final String PATH_PRIZES_PAGE = "/api/provider/promotion/lottery/prizes/page";
    private static final String PATH_PRIZES_SAVE = "/api/provider/promotion/lottery/prizes/save";
    private static final String PATH_PRIZES_DELETE = "/api/provider/promotion/lottery/prizes/delete";
    private static final String PATH_ACTIVITIES_PAGE = "/api/provider/promotion/lottery/activities/page";
    private static final String PATH_ACTIVITIES_SAVE = "/api/provider/promotion/lottery/activities/save";
    private static final String PATH_ACTIVITIES_DELETE = "/api/provider/promotion/lottery/activities/delete";
    private static final String PATH_ACTIVITY_PRIZES_LIST = "/api/provider/promotion/lottery/activity-prizes/list";
    private static final String PATH_ACTIVITY_PRIZES_SAVE = "/api/provider/promotion/lottery/activity-prizes/save";
    private static final String PATH_ACTIVITY_PRIZES_DELETE = "/api/provider/promotion/lottery/activity-prizes/delete";
    private static final String PATH_RECORDS_PAGE = "/api/provider/promotion/lottery/records/page";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<PromotionPrizeResp> prizePage(PromotionPrizeQueryReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_PRIZES_PAGE), req,
                new TypeReference<Page<PromotionPrizeResp>>() {});
    }

    @Override
    public PromotionPrizeResp savePrize(PromotionPrizeReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_PRIZES_SAVE), req, PromotionPrizeResp.class);
    }

    @Override
    public void deletePrize(Long id) throws BizException {
        apiRestClient.post(promotionUrl(PATH_PRIZES_DELETE), idReq(id), Void.class);
    }

    @Override
    public Page<PromotionLotteryActivityResp> activityPage(PromotionLotteryActivityQueryReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_ACTIVITIES_PAGE), req,
                new TypeReference<Page<PromotionLotteryActivityResp>>() {});
    }

    @Override
    public PromotionLotteryActivityResp saveActivity(PromotionLotteryActivityReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_ACTIVITIES_SAVE), req, PromotionLotteryActivityResp.class);
    }

    @Override
    public void deleteActivity(Long id) throws BizException {
        apiRestClient.post(promotionUrl(PATH_ACTIVITIES_DELETE), idReq(id), Void.class);
    }

    @Override
    public List<PromotionLotteryPrizeResp> activityPrizes(Long activityId) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_ACTIVITY_PRIZES_LIST), idReq(activityId),
                new TypeReference<List<PromotionLotteryPrizeResp>>() {});
    }

    @Override
    public PromotionLotteryPrizeResp saveActivityPrize(PromotionLotteryPrizeReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_ACTIVITY_PRIZES_SAVE), req, PromotionLotteryPrizeResp.class);
    }

    @Override
    public void deleteActivityPrize(Long id) throws BizException {
        apiRestClient.post(promotionUrl(PATH_ACTIVITY_PRIZES_DELETE), idReq(id), Void.class);
    }

    @Override
    public Page<PromotionLotteryRecordResp> recordPage(PromotionLotteryRecordQueryReq req) throws BizException {
        return apiRestClient.post(promotionUrl(PATH_RECORDS_PAGE), req,
                new TypeReference<Page<PromotionLotteryRecordResp>>() {});
    }

    private IdReq idReq(Long id) {
        IdReq req = new IdReq();
        req.setId(id);
        return req;
    }

    private String promotionUrl(String path) {
        return serviceEndpoints.getPromotionBaseUrl() + path;
    }
}