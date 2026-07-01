package com.mall.admin.service.wallet.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.wallet.WalletFlowAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WalletFlowResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/** 账务流水管理端转发实现：admin 只聚合展示，不持有账务状态。 */
@Service
public class WalletFlowAdminServiceImpl implements WalletFlowAdminService {

    private static final String PATH_LIST = "/api/provider/wallet/flow/list";
    private static final int DEFAULT_LIMIT = 200;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<WalletFlowResp> list(Long userId, Integer limit) throws BizException {
        StringBuilder url = new StringBuilder(walletUrl(PATH_LIST))
                .append("?limit=").append(limit == null || limit <= 0 ? DEFAULT_LIMIT : limit);
        if (userId != null && userId > 0) {
            url.append("&userId=").append(userId);
        }
        return apiRestClient.get(url.toString(), new TypeReference<List<WalletFlowResp>>() {});
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}