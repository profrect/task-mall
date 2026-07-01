package com.mall.admin.controller.collect;

import com.mall.admin.service.collect.CollectAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.CollectOrderResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 归集管理（管理端）。受 AdminLoginInterceptor + AdminPermissionInterceptor 双重拦截：
 * 需登录且具备该接口的 API 权限（超管自动放行）。
 * <p>
 * 归集 = 把分散在各用户充值地址上的 USDT 集中到出款热钱包；定时任务默认关闭，这里提供人工触发与监控。
 */
@RestController
@RequestMapping("/api/admin/collect")
public class CollectController {

    @Resource
    private CollectAdminService collectAdminService;

    /** 进行中归集订单（CREATED/GAS_FUNDING/SWEEPING），供监控面板。 */
    @GetMapping("/active")
    public Result<List<CollectOrderResp>> active() throws BizException {
        return Result.ok(collectAdminService.activeList());
    }

    /** 按状态查询归集订单（CREATED/GAS_FUNDING/SWEEPING/COMPLETED/FAILED）。 */
    @GetMapping("/list")
    public Result<List<CollectOrderResp>> list(@RequestParam String status) throws BizException {
        return Result.ok(collectAdminService.listByStatus(status));
    }

    /** 手工触发扫描建单：chainCode 可选，留空则扫所有已接入链。返回新建订单数。 */
    @PostMapping("/scan")
    public Result<Integer> scan(@RequestParam(required = false) String chainCode) throws BizException {
        return Result.ok(collectAdminService.scan(chainCode));
    }

    /** 手工推进所有在途归集订单一轮（喂 gas → 归集广播 → 确认）。 */
    @PostMapping("/advance")
    public Result<Void> advance() throws BizException {
        collectAdminService.advance();
        return Result.ok();
    }
}