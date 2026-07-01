package com.mall.admin.service.order;

import com.mall.admin.model.dto.OrderApprovalReviewDTO;
import com.mall.admin.model.vo.OrderApprovalItemVO;
import com.mall.common.core.exception.BizException;

import java.util.List;

/** 订单审核聚合服务：只编排不同业务域的真实待审核状态机。 */
public interface OrderApprovalService {

    List<OrderApprovalItemVO> pendingList() throws BizException;

    OrderApprovalItemVO approve(OrderApprovalReviewDTO dto) throws BizException;

    OrderApprovalItemVO reject(OrderApprovalReviewDTO dto) throws BizException;
}