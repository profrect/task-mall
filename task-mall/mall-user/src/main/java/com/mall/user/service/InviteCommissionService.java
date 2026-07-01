package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mybatisflex.core.paginate.Page;

public interface InviteCommissionService {

    Page<InviteCommissionRecordResp> page(InviteCommissionRecordQueryReq req) throws BizException;

    Page<InviteCommissionRecordResp> userPage(Long inviterUserId, BasePageDTO page) throws BizException;

    void settleVipUpgrade(String orderNo);
}