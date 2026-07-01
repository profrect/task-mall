package com.mall.wallet.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.CollectOrderResp;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.collect.CollectService;
import com.mall.wallet.collect.enums.CollectStatus;
import com.mall.wallet.collect.model.entity.CollectOrder;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * 归集 provider 接口（内部，仅供 mall-admin 经 ApiRestClient 调用）。
 * <p>
 * 归集 = 把分散在用户充值地址上的 USDT 集中到出款热钱包；定时任务默认关闭，本接口提供人工触发与监控。
 * <p>
 * <b>内部信任边界</b>：与 WithdrawProviderController 同——当前 {@code /api/provider/**} 仅由 RestClient 注入
 * {@code X-Inner-Token}，接收端尚无校验，网关 / 内网隔离落地前默认信任内网来源，后续应补统一的内部令牌校验过滤器。
 */
@RestController
@RequestMapping("/api/provider/wallet/collect")
public class CollectProviderController {

    @Resource
    private CollectService collectService;

    @Resource
    private ChainAdapterRegistry adapterRegistry;

    /**
     * 手工触发归集扫描建单：指定 chainCode 则扫该链，否则扫所有已接入的链。返回新建订单数。
     * 仅建单不广播；广播由后续推进（手工 /advance 或定时任务）完成。
     */
    @PostMapping("/scan")
    public Result<Integer> scan(@RequestParam(required = false) String chainCode) {
        int created = 0;
        if (StringUtils.hasText(chainCode)) {
            created = collectService.scanAndCreate(ChainCode.valueOf(chainCode.trim().toUpperCase(Locale.ROOT)));
        } else {
            for (ChainCode c : adapterRegistry.supportedChains()) {
                created += collectService.scanAndCreate(c);
            }
        }
        return Result.ok(created);
    }

    /** 手工推进所有在途归集订单一轮（喂 gas → 归集广播 → 确认）。 */
    @PostMapping("/advance")
    public Result<Void> advance() {
        collectService.advanceAll();
        return Result.ok();
    }

    /** 进行中归集订单（CREATED/GAS_FUNDING/SWEEPING），供管理端监控。 */
    @GetMapping("/active")
    public Result<List<CollectOrderResp>> active() {
        return Result.ok(collectService.listActive().stream().map(this::toResp).toList());
    }

    /** 按状态查询归集订单。 */
    @GetMapping("/list")
    public Result<List<CollectOrderResp>> list(@RequestParam String status) {
        List<CollectOrderResp> list = collectService.listByStatus(CollectStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)))
                .stream().map(this::toResp).toList();
        return Result.ok(list);
    }

    private CollectOrderResp toResp(CollectOrder o) {
        CollectOrderResp r = new CollectOrderResp();
        r.setOrderNo(o.getOrderNo());
        r.setUserId(o.getUserId());
        r.setChainCode(o.getChainCode());
        r.setCoin(o.getCoin());
        r.setFromAddress(o.getFromAddress());
        r.setToAddress(o.getToAddress());
        r.setAmount(o.getAmount());
        r.setSweptAmount(o.getSweptAmount());
        r.setGasTxHash(o.getGasTxHash());
        r.setSweepTxHash(o.getSweepTxHash());
        r.setConfirmations(o.getConfirmations());
        r.setStatus(o.getStatus());
        r.setRemark(o.getRemark());
        r.setGasSentAt(o.getGasSentAt());
        r.setSweptAt(o.getSweptAt());
        r.setFinishedAt(o.getFinishedAt());
        r.setCreateTime(o.getCreateTime());
        return r;
    }
}