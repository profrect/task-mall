package com.mall.wallet.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.WithdrawReviewReq;
import com.mall.common.model.dto.resp.WithdrawOrderResp;
import com.mall.wallet.withdraw.WithdrawService;
import com.mall.wallet.withdraw.enums.WithdrawStatus;
import com.mall.wallet.withdraw.model.entity.WalletWithdrawOrder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提现审核 provider 接口（内部，仅供 mall-admin 经 ApiRestClient 调用）。
 * <p>
 * 不解析 sa-token 会话、不读取用户身份——审核动作的发起者由调用方(admin)经 {@code reviewer} 带入。
 * <p>
 * <b>内部信任边界</b>：当前内部调用仅由 RestClient 注入 {@code X-Inner-Token}，接收端尚无校验
 * （全项目统一现状）。在网关 / 内网隔离落地前，{@code /api/provider/**} 默认信任内网来源，
 * 后续应补统一的内部令牌校验过滤器。
 */
@RestController
@RequestMapping("/api/provider/wallet/withdraw")
public class WithdrawProviderController {

    @Resource
    private WithdrawService withdrawService;

    /** 待审核列表（REVIEWING，按申请先后）。 */
    @GetMapping("/reviewing")
    public Result<List<WithdrawOrderResp>> reviewing() {
        List<WithdrawOrderResp> list = withdrawService.listByStatus(WithdrawStatus.REVIEWING).stream()
                .map(this::toResp)
                .toList();
        return Result.ok(list);
    }

    /** 审核通过 → 立即热钱包出款广播（广播失败将解冻退款并把原因抛回）。 */
    @PostMapping("/approve")
    public Result<WithdrawOrderResp> approve(@RequestBody WithdrawReviewReq req) throws BizException {
        WalletWithdrawOrder order = withdrawService.approve(req.getOrderNo(), req.getReviewer(), req.getRemark());
        return Result.ok(toResp(order));
    }

    /** 驳回 → 解冻退款。 */
    @PostMapping("/reject")
    public Result<WithdrawOrderResp> reject(@RequestBody WithdrawReviewReq req) throws BizException {
        WalletWithdrawOrder order = withdrawService.reject(req.getOrderNo(), req.getReviewer(), req.getRemark());
        return Result.ok(toResp(order));
    }

    private WithdrawOrderResp toResp(WalletWithdrawOrder o) {
        WithdrawOrderResp r = new WithdrawOrderResp();
        r.setOrderNo(o.getOrderNo());
        r.setUserId(o.getUserId());
        r.setChainCode(o.getChainCode());
        r.setCoin(o.getCoin());
        r.setAmount(o.getAmount());
        r.setFee(o.getFee());
        r.setReceiveAmount(o.getReceiveAmount());
        r.setToAddress(o.getToAddress());
        r.setFromAddress(o.getFromAddress());
        r.setTxHash(o.getTxHash());
        r.setConfirmations(o.getConfirmations());
        r.setStatus(o.getStatus());
        r.setReviewer(o.getReviewer());
        r.setReviewRemark(o.getReviewRemark());
        r.setReviewedAt(o.getReviewedAt());
        r.setBroadcastAt(o.getBroadcastAt());
        r.setFinishedAt(o.getFinishedAt());
        r.setCreateTime(o.getCreateTime());
        return r;
    }
}