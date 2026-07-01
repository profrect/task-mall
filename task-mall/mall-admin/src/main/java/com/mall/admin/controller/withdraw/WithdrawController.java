package com.mall.admin.controller.withdraw;

import com.mall.admin.model.dto.WithdrawReviewDTO;
import com.mall.admin.service.withdraw.WithdrawAuditService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.WithdrawOrderResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提现审核（管理端）。受 AdminLoginInterceptor + AdminPermissionInterceptor 双重拦截：
 * 需登录且具备该接口的 API 权限（超管自动放行）；审核人身份由服务端从登录态解析，不接受前端传入。
 */
@RestController
@RequestMapping("/api/admin/withdraw")
public class WithdrawController {

    @Resource
    private WithdrawAuditService withdrawAuditService;

    @GetMapping("/reviewing")
    public Result<List<WithdrawOrderResp>> reviewing() throws BizException {
        return Result.ok(withdrawAuditService.reviewingList());
    }

    @PostMapping("/approve")
    public Result<WithdrawOrderResp> approve(@RequestBody WithdrawReviewDTO dto) throws BizException {
        return Result.ok(withdrawAuditService.approve(dto));
    }

    @PostMapping("/reject")
    public Result<WithdrawOrderResp> reject(@RequestBody WithdrawReviewDTO dto) throws BizException {
        return Result.ok(withdrawAuditService.reject(dto));
    }
}