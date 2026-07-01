package com.mall.admin.service.recharge.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.recharge.RechargeQueryService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.RechargeOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 充值订单查询实现：薄编排层，仅做跨服务转发，不持有任何充值状态/账务逻辑（全部在 mall-wallet 内核）。
 * <p>
 * 与 CollectAdminServiceImpl 同构，但更简单——充值无触发动作，仅一个只读列表查询。
 */
@Slf4j
@Service
public class RechargeQueryServiceImpl implements RechargeQueryService {

    private static final String PATH_LIST = "/api/provider/wallet/recharge/list";
    private static final int DEFAULT_LIMIT = 200;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<RechargeOrderResp> list(String status, Integer limit) throws BizException {
        StringBuilder url = new StringBuilder(walletUrl(PATH_LIST))
                .append("?limit=").append(limit == null || limit <= 0 ? DEFAULT_LIMIT : limit);
        if (StringUtils.hasText(status)) {
            url.append("&status=").append(status.trim());
        }
        return apiRestClient.get(url.toString(), new TypeReference<List<RechargeOrderResp>>() {});
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}