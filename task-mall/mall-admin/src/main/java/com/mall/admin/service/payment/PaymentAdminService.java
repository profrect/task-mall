package com.mall.admin.service.payment;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.PaymentOrderQueryReq;
import com.mall.common.model.dto.resp.PaymentOrderResp;
import com.mybatisflex.core.paginate.Page;

/** 支付订单审计查询：管理端只聚合展示，不直接改钱包。 */
public interface PaymentAdminService {

    Page<PaymentOrderResp> page(PaymentOrderQueryReq req) throws BizException;
}