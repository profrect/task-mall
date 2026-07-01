package com.mall.admin.service.payment.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.service.payment.PaymentAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.PaymentOrderQueryReq;
import com.mall.common.model.dto.resp.PaymentOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** 支付审计管理端转发实现：支付事实由 mall-wallet 持有。 */
@Service
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private static final String PATH_PAGE = "/api/provider/wallet/payment/page";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<PaymentOrderResp> page(PaymentOrderQueryReq req) throws BizException {
        PaymentOrderQueryReq query = req == null ? new PaymentOrderQueryReq() : req;
        return apiRestClient.post(walletUrl(PATH_PAGE), query,
                new TypeReference<Page<PaymentOrderResp>>() {});
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}