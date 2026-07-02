package com.mall.wallet.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.RechargeOrderResp;
import com.mall.wallet.recharge.RechargeService;
import com.mall.wallet.recharge.model.entity.WalletRechargeOrder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 充值订单 provider 接口（内部，仅供 mall-admin 经 ApiRestClient 调用）。
 * <p>
 * 充值为链上自动入账（扫块达确认数即记账），管理端只做只读监控，无审批/触发动作——
 * 与 withdraw（需审批）、collect（需触发）不同，本接口仅暴露查询。
 * <p>
 * <b>内部信任边界</b>：与 WithdrawProviderController / CollectProviderController 同——当前
 * {@code /api/provider/**} 仅由 RestClient 注入 {@code X-Inner-Token}，接收端尚无校验，网关 / 内网
 * 隔离落地前默认信任内网来源，后续应补统一的内部令牌校验过滤器。
 */
@RestController
@RequestMapping("/api/provider/wallet/recharge")
public class RechargeProviderController {

    /** 管理端列表默认/最大上限，防止全表扫描压垮接口。 */
    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;

    @Resource
    private RechargeService rechargeService;

    /**
     * 充值订单列表：status 可选（CONFIRMING / CREDITED），留空查全部；最新在前，限量返回。
     */
    @GetMapping("/list")
    public Result<List<RechargeOrderResp>> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false, defaultValue = "200") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        List<RechargeOrderResp> list = rechargeService.listForAdmin(status, safeLimit).stream()
                .map(this::toResp)
                .toList();
        return Result.ok(list);
    }

    private RechargeOrderResp toResp(WalletRechargeOrder o) {
        RechargeOrderResp r = new RechargeOrderResp();
        r.setOrderNo(o.getOrderNo());
        r.setUserId(o.getUserId());
        r.setChainCode(o.getChainCode());
        r.setCoin(o.getCoin());
        r.setAmount(o.getAmount());
        r.setFromAddress(o.getFromAddress());
        r.setToAddress(o.getToAddress());
        r.setTxHash(o.getTxHash());
        r.setConfirmations(o.getConfirmations());
        r.setStatus(o.getStatus());
        r.setCreditedAt(o.getCreditedAt());
        r.setCreateTime(o.getCreateTime());
        return r;
    }
}