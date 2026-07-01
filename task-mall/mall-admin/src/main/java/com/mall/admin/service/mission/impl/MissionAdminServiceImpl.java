package com.mall.admin.service.mission.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.mission.MissionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** 管理端任务中心聚合：任务事实由 mall-mission 持有。 */
@Service
public class MissionAdminServiceImpl implements MissionAdminService {

    private static final String PATH_GOODS_PAGE = "/api/provider/mission/goods/page";
    private static final String PATH_GOODS_SAVE = "/api/provider/mission/goods/save";
    private static final String PATH_GOODS_DELETE = "/api/provider/mission/goods/delete";
    private static final String PATH_TASKS_PAGE = "/api/provider/mission/tasks/page";
    private static final String PATH_TASKS_SAVE = "/api/provider/mission/tasks/save";
    private static final String PATH_TASKS_DELETE = "/api/provider/mission/tasks/delete";
    private static final String PATH_RECORDS_PAGE = "/api/provider/mission/records/page";
    private static final String PATH_RECORDS_APPROVE = "/api/provider/mission/records/approve";
    private static final String PATH_RECORDS_REJECT = "/api/provider/mission/records/reject";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<MissionGoodsResp> goodsPage(MissionGoodsQueryReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_GOODS_PAGE), req,
                new TypeReference<Page<MissionGoodsResp>>() {});
    }

    @Override
    public MissionGoodsResp saveGoods(MissionGoodsReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_GOODS_SAVE), req, MissionGoodsResp.class);
    }

    @Override
    public void deleteGoods(Long id) throws BizException {
        IdReq req = new IdReq();
        req.setId(id);
        apiRestClient.post(missionUrl(PATH_GOODS_DELETE), req, Void.class);
    }

    @Override
    public Page<MissionTaskResp> taskPage(MissionTaskQueryReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_TASKS_PAGE), req,
                new TypeReference<Page<MissionTaskResp>>() {});
    }

    @Override
    public MissionTaskResp saveTask(MissionTaskReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_TASKS_SAVE), req, MissionTaskResp.class);
    }

    @Override
    public void deleteTask(Long id) throws BizException {
        IdReq req = new IdReq();
        req.setId(id);
        apiRestClient.post(missionUrl(PATH_TASKS_DELETE), req, Void.class);
    }

    @Override
    public Page<MissionUserTaskResp> recordPage(MissionUserTaskQueryReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_RECORDS_PAGE), req,
                new TypeReference<Page<MissionUserTaskResp>>() {});
    }

    @Override
    public MissionUserTaskResp approve(MissionTaskReviewReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_RECORDS_APPROVE), req, MissionUserTaskResp.class);
    }

    @Override
    public MissionUserTaskResp reject(MissionTaskReviewReq req) throws BizException {
        return apiRestClient.post(missionUrl(PATH_RECORDS_REJECT), req, MissionUserTaskResp.class);
    }

    private String missionUrl(String path) {
        return serviceEndpoints.getMissionBaseUrl() + path;
    }
}