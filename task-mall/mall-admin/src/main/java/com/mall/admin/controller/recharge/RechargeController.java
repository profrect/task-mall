package com.mall.admin.controller.recharge;

import com.mall.admin.model.dto.RechargeManualDTO;
import com.mall.admin.service.recharge.RechargeManualService;
import com.mall.admin.service.recharge.RechargeQueryService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.RechargeOrderResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 充值订单查询（管理端）。受 AdminLoginInterceptor + AdminPermissionInterceptor 双重拦截：
 * 需登录且具备该接口的 API 权限（超管自动放行）。
 * <p>
 * 充值是链上自动入账，管理端为只读监控面板——无审批/触发动作，仅按状态/限量查询。
 */
@RestController
@RequestMapping("/api/admin/recharge")
public class RechargeController {

    @Resource
    private RechargeQueryService rechargeQueryService;

    @Resource
    private RechargeManualService rechargeManualService;

    /** 充值订单列表：status 可选（CONFIRMING/CREDITED），留空查全部；最新在前，limit 限量。 */
    @GetMapping("/list")
    public Result<List<RechargeOrderResp>> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "limit", required = false) Integer limit) throws BizException {
        return Result.ok(rechargeQueryService.list(status, userId, limit));
    }

    /** 人工充值补单：服务端注入当前管理员，wallet 域单事务完成订单、审计和入账。 */
    @PostMapping("/manual-credit")
    public Result<RechargeOrderResp> manualCredit(@RequestBody RechargeManualDTO dto) throws BizException {
        return Result.ok(rechargeManualService.manualCredit(dto));
    }
}