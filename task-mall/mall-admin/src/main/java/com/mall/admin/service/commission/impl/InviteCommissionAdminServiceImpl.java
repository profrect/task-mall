package com.mall.admin.service.commission.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.commission.InviteCommissionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** 后台邀请返佣聚合：返佣事实由 mall-user 持有，后台只做查询转发。 */
@Service
public class InviteCommissionAdminServiceImpl implements InviteCommissionAdminService {

    private static final String PATH_PAGE = "/api/provider/user/invite/commission/page";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<InviteCommissionRecordResp> page(InviteCommissionRecordQueryReq req) throws BizException {
        return apiRestClient.post(userUrl(PATH_PAGE), req,
                new TypeReference<Page<InviteCommissionRecordResp>>() {});
    }

    private String userUrl(String path) {
        return serviceEndpoints.getUserBaseUrl() + path;
    }
}