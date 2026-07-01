package com.mall.admin.controller.payment;

import com.mall.admin.service.payment.PaymentAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.PaymentOrderQueryReq;
import com.mall.common.model.dto.resp.PaymentOrderResp;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 支付订单审计：只读查询，不触发钱包入账。 */
@RestController
@RequestMapping("/api/admin/payment")
public class PaymentAdminController {

    @Resource
    private PaymentAdminService paymentAdminService;

    @GetMapping("/list")
    public Result<Page<PaymentOrderResp>> list(PaymentOrderQueryReq req) throws BizException {
        return Result.ok(paymentAdminService.page(req));
    }
}