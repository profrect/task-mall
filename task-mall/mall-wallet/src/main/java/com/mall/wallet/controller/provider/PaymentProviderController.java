package com.mall.wallet.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.PaymentOrderQueryReq;
import com.mall.common.model.dto.resp.PaymentOrderResp;
import com.mall.wallet.payment.PaymentOrderQueryService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 支付审计 provider：内部只读查询，不提供任何余额变更能力。 */
@RestController
@RequestMapping("/api/provider/wallet/payment")
public class PaymentProviderController {

    @Resource
    private PaymentOrderQueryService paymentOrderQueryService;

    @PostMapping("/page")
    public Result<Page<PaymentOrderResp>> page(@RequestBody(required = false) PaymentOrderQueryReq req) {
        return Result.ok(paymentOrderQueryService.page(req));
    }
}