package com.mall.admin.service.vip.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.vip.VipAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理端 VIP 配置聚合：不直连用户库，统一走用户域 provider。 */
@Service
public class VipAdminServiceImpl implements VipAdminService {

    private static final String PATH_LIST = "/api/provider/user/vip/config/list";
    private static final String PATH_SAVE = "/api/provider/user/vip/config/save";
    private static final String PATH_DELETE = "/api/provider/user/vip/config/delete";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<VipLevelConfigResp> list(Integer status) throws BizException {
        String url = userUrl(PATH_LIST) + (status == null ? "" : "?status=" + status);
        return apiRestClient.get(url, new TypeReference<List<VipLevelConfigResp>>() {});
    }

    @Override
    public VipLevelConfigResp save(VipLevelConfigReq req) throws BizException {
        return apiRestClient.post(userUrl(PATH_SAVE), req, VipLevelConfigResp.class);
    }

    @Override
    public void delete(Long id) throws BizException {
        IdReq req = new IdReq();
        req.setId(id);
        apiRestClient.post(userUrl(PATH_DELETE), req, Void.class);
    }

    private String userUrl(String path) {
        return serviceEndpoints.getUserBaseUrl() + path;
    }
}