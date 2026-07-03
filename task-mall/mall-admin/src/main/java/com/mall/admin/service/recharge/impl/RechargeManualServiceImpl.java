package com.mall.admin.service.recharge.impl;

import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.model.dto.RechargeManualDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.recharge.RechargeManualService;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.RechargeManualReq;
import com.mall.common.model.dto.resp.RechargeOrderResp;
import com.mall.common.web.rest.ApiRestClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 人工充值补单聚合实现。
 *
 * 设计边界：
 * - 会员是否存在由 admin 聚合层向 mall-user 校验；
 * - 操作人由 UserContext 注入，前端无权指定；
 * - 充值订单、支付审计和余额变动全部由 mall-wallet 单事务完成。
 */
@Slf4j
@Service
public class RechargeManualServiceImpl implements RechargeManualService {

    private static final String PATH_MANUAL_CREDIT = "/api/provider/wallet/recharge/manual-credit";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Resource
    private UserService userService;

    @Override
    public RechargeOrderResp manualCredit(RechargeManualDTO dto) throws BizException {
        Preconditions.notNull(dto, AdminRespCodeEnum.DATA_NOT_FOUND, "补单请求");
        Preconditions.needTrue(dto.getUserId() != null && dto.getUserId() > 0,
                AdminRespCodeEnum.DATA_NOT_FOUND, "会员UID");
        UserInfoVO user = userService.userDetail(dto.getUserId());
        Preconditions.notNull(user, AdminRespCodeEnum.USER_NOT_EXIST);
        return apiRestClient.post(walletUrl(PATH_MANUAL_CREDIT), toReq(dto), RechargeOrderResp.class);
    }

    private RechargeManualReq toReq(RechargeManualDTO dto) {
        RechargeManualReq req = new RechargeManualReq();
        req.setUserId(dto.getUserId());
        req.setAmount(dto.getAmount());
        req.setCoin(dto.getCoin());
        req.setReferenceNo(dto.getReferenceNo());
        req.setRemark(dto.getRemark());
        req.setOperator(UserContext.currentUsername());
        return req;
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}