package com.mall.admin.service.transfer.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.transfer.TransferAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WalletTransferOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理端转账订单聚合：订单事实仍由 mall-wallet 持有。 */
@Service
public class TransferAdminServiceImpl implements TransferAdminService {

    private static final String PATH_LIST = "/api/provider/wallet/transfer/list";
    private static final int DEFAULT_LIMIT = 200;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<WalletTransferOrderResp> list(Long userId, Integer limit) throws BizException {
        StringBuilder url = new StringBuilder(walletUrl(PATH_LIST))
                .append("?limit=").append(limit == null || limit <= 0 ? DEFAULT_LIMIT : limit);
        if (userId != null && userId > 0) {
            url.append("&userId=").append(userId);
        }
        return apiRestClient.get(url.toString(), new TypeReference<List<WalletTransferOrderResp>>() {});
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}