package com.mall.admin.service.order.impl;

import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.model.dto.OrderApprovalReviewDTO;
import com.mall.admin.model.dto.WithdrawReviewDTO;
import com.mall.admin.model.vo.OrderApprovalItemVO;
import com.mall.admin.service.mission.MissionAdminService;
import com.mall.admin.service.order.OrderApprovalService;
import com.mall.admin.service.withdraw.WithdrawAuditService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.common.model.dto.resp.WithdrawOrderResp;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 统一订单审核待办：提现审核属于钱包域，任务提交审核属于任务域；本层只做聚合和路由。
 */
@Service
public class OrderApprovalServiceImpl implements OrderApprovalService {

    private static final String SOURCE_WITHDRAW = "WITHDRAW";
    private static final String SOURCE_MISSION_TASK = "MISSION_TASK";
    private static final int MISSION_PENDING_LIMIT = 100;

    @Resource
    private WithdrawAuditService withdrawAuditService;

    @Resource
    private MissionAdminService missionAdminService;

    @Override
    public List<OrderApprovalItemVO> pendingList() throws BizException {
        List<OrderApprovalItemVO> items = new ArrayList<>();
        withdrawAuditService.reviewingList().stream()
                .map(this::fromWithdraw)
                .forEach(items::add);
        missionPending().stream()
                .map(this::fromMission)
                .forEach(items::add);
        items.sort(Comparator.comparing(OrderApprovalItemVO::getSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }

    @Override
    public OrderApprovalItemVO approve(OrderApprovalReviewDTO dto) throws BizException {
        return switch (source(dto)) {
            case SOURCE_WITHDRAW -> fromWithdraw(withdrawAuditService.approve(toWithdrawReview(dto)));
            case SOURCE_MISSION_TASK -> fromMission(missionAdminService.approve(toMissionReview(dto)));
            default -> throw new BizException(AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID,
                    new Object[]{dto == null ? null : dto.getSourceType()});
        };
    }

    @Override
    public OrderApprovalItemVO reject(OrderApprovalReviewDTO dto) throws BizException {
        return switch (source(dto)) {
            case SOURCE_WITHDRAW -> fromWithdraw(withdrawAuditService.reject(toWithdrawReview(dto)));
            case SOURCE_MISSION_TASK -> fromMission(missionAdminService.reject(toMissionReview(dto)));
            default -> throw new BizException(AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID,
                    new Object[]{dto == null ? null : dto.getSourceType()});
        };
    }

    private List<MissionUserTaskResp> missionPending() throws BizException {
        MissionUserTaskQueryReq req = new MissionUserTaskQueryReq();
        req.setStatus("SUBMITTED");
        req.setPageNumber(1);
        req.setPageSize(MISSION_PENDING_LIMIT);
        Page<MissionUserTaskResp> page = missionAdminService.recordPage(req);
        return page == null || page.getRecords() == null ? List.of() : page.getRecords();
    }

    private String source(OrderApprovalReviewDTO dto) throws BizException {
        Preconditions.notNull(dto, AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID);
        Preconditions.needTrue(StringUtils.hasText(dto.getSourceType()),
                AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID, dto.getSourceType());
        Preconditions.needTrue(StringUtils.hasText(dto.getSourceId()),
                AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID, dto.getSourceType());
        return dto.getSourceType().trim().toUpperCase(Locale.ROOT);
    }

    private WithdrawReviewDTO toWithdrawReview(OrderApprovalReviewDTO dto) {
        WithdrawReviewDTO req = new WithdrawReviewDTO();
        req.setOrderNo(dto.getSourceId());
        req.setRemark(dto.getRemark());
        return req;
    }

    private MissionTaskReviewReq toMissionReview(OrderApprovalReviewDTO dto) throws BizException {
        try {
            MissionTaskReviewReq req = new MissionTaskReviewReq();
            req.setRecordId(Long.parseLong(dto.getSourceId()));
            req.setReviewRemark(dto.getRemark());
            return req;
        } catch (NumberFormatException e) {
            throw new BizException(AdminRespCodeEnum.ORDER_APPROVAL_SOURCE_INVALID, new Object[]{dto.getSourceId()});
        }
    }

    private OrderApprovalItemVO fromWithdraw(WithdrawOrderResp order) {
        return OrderApprovalItemVO.builder()
                .sourceType(SOURCE_WITHDRAW)
                .sourceId(order.getOrderNo())
                .title("提现审核")
                .userId(order.getUserId())
                .currency(order.getCoin())
                .amount(nz(order.getAmount()))
                .status(order.getStatus())
                .detail(order.getChainCode() + " / " + order.getToAddress())
                .submittedAt(order.getCreateTime())
                .createTime(order.getCreateTime())
                .build();
    }

    private OrderApprovalItemVO fromMission(MissionUserTaskResp record) {
        return OrderApprovalItemVO.builder()
                .sourceType(SOURCE_MISSION_TASK)
                .sourceId(String.valueOf(record.getId()))
                .title(record.getTaskTitle())
                .userId(record.getUserId())
                .currency(record.getCurrency())
                .amount(nz(record.getRewardAmount()))
                .status(record.getStatus())
                .detail(record.getSubmitContent())
                .submittedAt(record.getSubmittedAt())
                .createTime(record.getCreateTime())
                .build();
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}