package com.mall.admin.service.withdraw;

import com.mall.admin.model.dto.WithdrawReviewDTO;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WithdrawOrderResp;

import java.util.List;

/**
 * 提现审核编排（管理端）：把管理端动作翻译为对 mall-wallet provider 的跨服务调用。
 */
public interface WithdrawAuditService {

    /** 待审核列表。 */
    List<WithdrawOrderResp> reviewingList() throws BizException;

    /** 审核通过 → 触发热钱包出款。 */
    WithdrawOrderResp approve(WithdrawReviewDTO dto) throws BizException;

    /** 驳回 → 解冻退款。 */
    WithdrawOrderResp reject(WithdrawReviewDTO dto) throws BizException;
}