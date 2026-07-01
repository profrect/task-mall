package com.mall.wallet.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.WalletTransferOrderResp;
import com.mall.wallet.transfer.TransferService;
import com.mall.wallet.transfer.model.entity.WalletTransferOrder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 站内转账 provider：管理端只读查询转账订单，不提供人工改账入口。 */
@RestController
@RequestMapping("/api/provider/wallet/transfer")
public class TransferProviderController {

    @Resource
    private TransferService transferService;

    @GetMapping("/list")
    public Result<List<WalletTransferOrderResp>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "200") Integer limit) {
        return Result.ok(transferService.listForAdmin(userId, limit)
                .stream()
                .map(this::toResp)
                .toList());
    }

    private WalletTransferOrderResp toResp(WalletTransferOrder o) {
        WalletTransferOrderResp resp = new WalletTransferOrderResp();
        resp.setOrderNo(o.getOrderNo());
        resp.setFromUserId(o.getFromUserId());
        resp.setToUserId(o.getToUserId());
        resp.setCoin(o.getCoin());
        resp.setAmount(o.getAmount());
        resp.setStatus(o.getStatus());
        resp.setRemark(o.getRemark());
        resp.setFinishedAt(o.getFinishedAt());
        resp.setCreateTime(o.getCreateTime());
        return resp;
    }
}