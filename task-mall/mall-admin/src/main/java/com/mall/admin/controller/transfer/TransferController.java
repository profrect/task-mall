package com.mall.admin.controller.transfer;

import com.mall.admin.service.transfer.TransferAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.WalletTransferOrderResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 站内转账订单（管理端）：只读审计，不提供人工改账。 */
@RestController
@RequestMapping("/api/admin/transfer")
public class TransferController {

    @Resource
    private TransferAdminService transferAdminService;

    @GetMapping("/list")
    public Result<List<WalletTransferOrderResp>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer limit) throws BizException {
        return Result.ok(transferAdminService.list(userId, limit));
    }
}