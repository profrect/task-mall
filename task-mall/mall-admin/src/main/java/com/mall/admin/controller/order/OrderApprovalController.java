package com.mall.admin.controller.order;

import com.mall.admin.model.dto.OrderApprovalReviewDTO;
import com.mall.admin.model.vo.OrderApprovalItemVO;
import com.mall.admin.service.order.OrderApprovalService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 订单审核聚合接口：统一展示提现待审与任务提交待审。 */
@RestController
@RequestMapping("/api/admin/order-approval")
public class OrderApprovalController {

    @Resource
    private OrderApprovalService orderApprovalService;

    @GetMapping("/pending")
    public Result<List<OrderApprovalItemVO>> pending() throws BizException {
        return Result.ok(orderApprovalService.pendingList());
    }

    @PostMapping("/approve")
    public Result<OrderApprovalItemVO> approve(@RequestBody OrderApprovalReviewDTO dto) throws BizException {
        return Result.ok(orderApprovalService.approve(dto));
    }

    @PostMapping("/reject")
    public Result<OrderApprovalItemVO> reject(@RequestBody OrderApprovalReviewDTO dto) throws BizException {
        return Result.ok(orderApprovalService.reject(dto));
    }
}