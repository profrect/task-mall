package com.mall.admin.service.collect.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.collect.CollectAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.CollectOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 归集编排实现：薄编排层，仅做跨服务转发，不持有任何归集状态/账务逻辑（全部在 mall-wallet 内核）。
 * <p>
 * 与 {@code WithdrawAuditServiceImpl} 同构，但归集触发<strong>无需 reviewer 身份注入</strong>——
 * 归集是平台内部资金搬运而非审批动作，触发者权限由 AdminPermissionInterceptor 在入口把关即可。
 */
@Slf4j
@Service
public class CollectAdminServiceImpl implements CollectAdminService {

    private static final String PATH_ACTIVE = "/api/provider/wallet/collect/active";
    private static final String PATH_LIST = "/api/provider/wallet/collect/list";
    private static final String PATH_SCAN = "/api/provider/wallet/collect/scan";
    private static final String PATH_ADVANCE = "/api/provider/wallet/collect/advance";

    /**
     * scan/advance 是 POST 触发但无请求体，wallet 端按 query/无参读取；
     * 而 {@link ApiRestClient#post} 的 RestClient.body(..) 不接受 null，故以空对象占位。
     */
    private static final Map<String, Object> EMPTY_BODY = Map.of();

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<CollectOrderResp> activeList() throws BizException {
        return apiRestClient.get(walletUrl(PATH_ACTIVE),
                new TypeReference<List<CollectOrderResp>>() {});
    }

    @Override
    public List<CollectOrderResp> listByStatus(String status) throws BizException {
        return apiRestClient.get(walletUrl(PATH_LIST) + "?status=" + status,
                new TypeReference<List<CollectOrderResp>>() {});
    }

    @Override
    public int scan(String chainCode) throws BizException {
        String url = walletUrl(PATH_SCAN);
        if (StringUtils.hasText(chainCode)) {
            url = url + "?chainCode=" + chainCode.trim();
        }
        Integer created = apiRestClient.post(url, EMPTY_BODY, Integer.class);
        return created == null ? 0 : created;
    }

    @Override
    public void advance() throws BizException {
        apiRestClient.post(walletUrl(PATH_ADVANCE), EMPTY_BODY, Void.class);
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}