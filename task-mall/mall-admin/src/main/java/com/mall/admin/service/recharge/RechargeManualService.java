package com.mall.admin.service.recharge;

import com.mall.admin.model.dto.RechargeManualDTO;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.RechargeOrderResp;

/**
 * 充值人工补单聚合服务。
 *
 * mall-admin 只负责权限、登录态和会员存在性校验；真正的订单和账务状态机落在 mall-wallet。
 */
public interface RechargeManualService {

    RechargeOrderResp manualCredit(RechargeManualDTO dto) throws BizException;
}