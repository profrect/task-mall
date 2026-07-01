package com.mall.admin.service.commission;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mybatisflex.core.paginate.Page;

public interface InviteCommissionAdminService {

    Page<InviteCommissionRecordResp> page(InviteCommissionRecordQueryReq req) throws BizException;
}