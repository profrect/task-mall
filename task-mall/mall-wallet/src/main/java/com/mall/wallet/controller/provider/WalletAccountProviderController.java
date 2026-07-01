package com.mall.wallet.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.WalletAccountBatchReq;
import com.mall.common.model.dto.resp.WalletAccountResp;
import com.mall.common.model.dto.resp.WalletStatsResp;
import com.mall.wallet.model.entity.WalletAccount;
import com.mall.wallet.service.WalletAccountService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包账户 provider 接口（内部，仅供 mall-admin 聚合会员列表等只读场景调用）。
 * <p>
 * 只暴露余额快照，不提供任何改余额动作；账户懒创建仍只发生在用户侧钱包访问或账务事件中。
 */
@RestController
@RequestMapping("/api/provider/wallet/account")
public class WalletAccountProviderController {

    @Resource
    private WalletAccountService walletAccountService;

    /** 批量查询余额快照；未开户用户不会被创建，由调用方按 0 余额展示。 */
    @PostMapping("/batch")
    public Result<List<WalletAccountResp>> batch(@RequestBody WalletAccountBatchReq req) throws BizException {
        List<WalletAccountResp> list = walletAccountService.listByUserIds(req.getUserIds(), req.getCurrency())
                .stream()
                .map(this::toResp)
                .toList();
        return Result.ok(list);
    }

    /** 钱包资金统计快照；todayStartAt 由聚合方传入，统一报表自然日口径。 */
    @GetMapping("/stats")
    public Result<WalletStatsResp> stats(
            @RequestParam(required = false, defaultValue = "USDT") String currency,
            @RequestParam Long todayStartAt) throws BizException {
        return Result.ok(walletAccountService.stats(currency, todayStartAt));
    }

    private WalletAccountResp toResp(WalletAccount a) {
        WalletAccountResp r = new WalletAccountResp();
        r.setUserId(a.getUserId());
        r.setCurrency(a.getCurrency());
        r.setTotalBalance(a.getTotalBalance());
        r.setAvailBalance(a.getAvailBalance());
        r.setFrozenBalance(a.getFrozenBalance());
        return r;
    }
}