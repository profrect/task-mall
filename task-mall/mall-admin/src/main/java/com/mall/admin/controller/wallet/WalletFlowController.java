package com.mall.admin.controller.wallet;

import com.mall.admin.service.wallet.WalletFlowAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.WalletFlowResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包账务流水（管理端）。受 AdminLoginInterceptor + AdminPermissionInterceptor 双重拦截。
 * 该接口只读审计流水，不提供任何余额调整能力。
 */
@RestController
@RequestMapping("/api/admin/wallet-flow")
public class WalletFlowController {

    @Resource
    private WalletFlowAdminService walletFlowAdminService;

    /** 全站账务流水：userId 可选，最新在前，limit 限量。 */
    @GetMapping("/list")
    public Result<List<WalletFlowResp>> list(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "limit", required = false) Integer limit) throws BizException {
        return Result.ok(walletFlowAdminService.list(userId, limit));
    }
}