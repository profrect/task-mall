package com.mall.admin.controller.commission;

import com.mall.admin.service.commission.InviteCommissionAdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 邀请返佣后台审计列表。 */
@RestController
@RequestMapping("/api/admin/invite-commission")
public class InviteCommissionAdminController {

    @Resource
    private InviteCommissionAdminService inviteCommissionAdminService;

    @GetMapping("/list")
    public Result<Page<InviteCommissionRecordResp>> page(InviteCommissionRecordQueryReq req) throws BizException {
        return Result.ok(inviteCommissionAdminService.page(req));
    }
}