package com.mall.wallet.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.wallet.service.WalletSettlementService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 写入型钱包结算 provider：任务/VIP/抽奖/返佣等业务服务统一通过此入口触发真实账变。 */
@RestController
@RequestMapping("/api/provider/wallet/settlement")
public class WalletSettlementProviderController {

    @Resource
    private WalletSettlementService walletSettlementService;

    @PostMapping("/apply")
    public Result<WalletSettlementResp> apply(@RequestBody WalletSettlementReq req) throws BizException {
        return Result.ok(walletSettlementService.apply(req));
    }
}