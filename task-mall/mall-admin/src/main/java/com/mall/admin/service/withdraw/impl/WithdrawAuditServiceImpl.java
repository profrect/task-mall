package com.mall.admin.service.withdraw.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.model.dto.WithdrawReviewDTO;
import com.mall.admin.service.withdraw.WithdrawAuditService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.WithdrawReviewReq;
import com.mall.common.model.dto.resp.WithdrawOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提现审核编排实现：薄编排层，仅做「登录态 → reviewer 注入」与「跨服务转发」。
 * <p>
 * reviewer 在此从 {@link UserContext} 解析后注入请求，保证审核人可信、不可被前端伪造；
 * 真正的状态机迁移与账务（冻结/出款/解冻/结算）全部落在 mall-wallet 内核，管理端不感知。
 */
@Slf4j
@Service
public class WithdrawAuditServiceImpl implements WithdrawAuditService {

    private static final String PATH_REVIEWING = "/api/provider/wallet/withdraw/reviewing";
    private static final String PATH_APPROVE = "/api/provider/wallet/withdraw/approve";
    private static final String PATH_REJECT = "/api/provider/wallet/withdraw/reject";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<WithdrawOrderResp> reviewingList() throws BizException {
        return apiRestClient.get(walletUrl(PATH_REVIEWING),
                new TypeReference<List<WithdrawOrderResp>>() {});
    }

    @Override
    public WithdrawOrderResp approve(WithdrawReviewDTO dto) throws BizException {
        return apiRestClient.post(walletUrl(PATH_APPROVE), toReq(dto), WithdrawOrderResp.class);
    }

    @Override
    public WithdrawOrderResp reject(WithdrawReviewDTO dto) throws BizException {
        return apiRestClient.post(walletUrl(PATH_REJECT), toReq(dto), WithdrawOrderResp.class);
    }

    /** 审核人取自登录态，绝不取自前端入参。 */
    private WithdrawReviewReq toReq(WithdrawReviewDTO dto) {
        WithdrawReviewReq req = new WithdrawReviewReq();
        req.setOrderNo(dto.getOrderNo());
        req.setReviewer(UserContext.currentUsername());
        req.setRemark(dto.getRemark());
        return req;
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}