package com.mall.user.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.UserAdminSaveReq;
import com.mall.common.model.dto.req.UserGroupAssignReq;
import com.mall.common.model.dto.req.UserGroupReq;
import com.mall.common.model.dto.req.UserImpersonationTicketReq;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.UserBatchStatusUpdateReq;
import com.mall.common.model.dto.req.UserExistReq;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.req.UserLineChangeReq;
import com.mall.common.model.dto.req.UserProfileSummaryBatchReq;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.req.UserStatusUpdateReq;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserGroupResp;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mall.common.model.dto.resp.UserLineageResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import com.mall.user.service.InviteCommissionService;
import com.mall.user.service.UserImpersonationService;
import com.mall.user.service.UserInfoService;
import com.mall.user.service.UserMemberGroupService;
import com.mall.user.service.VipService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provider/user/")
public class UserProviderController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VipService vipService;

    @Resource
    private InviteCommissionService inviteCommissionService;

    @Resource
    private UserImpersonationService userImpersonationService;

    @Resource
    private UserMemberGroupService userMemberGroupService;

    @PostMapping("/user-page")
    public Result<Page<UserResp>> userPage(@RequestBody UserReq req){
        return Result.ok(userInfoService.pageList(req));
    }

    @GetMapping("/stats")
    public Result<UserStatsResp> stats(@RequestParam(name = "todayStartAt") Long todayStartAt) {
        return Result.ok(userInfoService.stats(todayStartAt));
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody UserStatusUpdateReq req) throws BizException {
        userInfoService.updateStatus(req.getUserId(), req.getStatus());
        return Result.ok();
    }

    @PostMapping("/batch-status")
    public Result<Void> updateStatusBatch(@RequestBody UserBatchStatusUpdateReq req) throws BizException {
        userInfoService.updateStatusBatch(req.getUserIds(), req.getStatus());
        return Result.ok();
    }

    @PostMapping("/detail")
    public Result<UserResp> detail(@RequestBody IdReq req) throws BizException {
        return Result.ok(userInfoService.detail(req.getId()));
    }

    @PostMapping("/admin-save")
    public Result<UserResp> adminSave(@RequestBody UserAdminSaveReq req) throws BizException {
        return Result.ok(userInfoService.adminSave(req));
    }

    @PostMapping("/lineage")
    public Result<UserLineageResp> lineage(@RequestBody IdReq req) throws BizException {
        return Result.ok(userInfoService.lineage(req.getId()));
    }

    @PostMapping("/lineage/change")
    public Result<Void> changeParent(@RequestBody UserLineChangeReq req) throws BizException {
        userInfoService.changeParent(req);
        return Result.ok();
    }

    @GetMapping("/group/list")
    public Result<List<UserGroupResp>> groupList(@RequestParam(name = "status", required = false) Integer status) {
        return Result.ok(userMemberGroupService.list(status));
    }

    @PostMapping("/group/save")
    public Result<UserGroupResp> saveGroup(@RequestBody UserGroupReq req) throws BizException {
        return Result.ok(userMemberGroupService.save(req));
    }

    @PostMapping("/group/delete")
    public Result<Void> deleteGroup(@RequestBody IdReq req) throws BizException {
        userMemberGroupService.delete(req.getId());
        return Result.ok();
    }

    @PostMapping("/group/assign")
    public Result<Void> assignGroup(@RequestBody UserGroupAssignReq req) throws BizException {
        userMemberGroupService.assign(req);
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody IdReq req) throws BizException {
        userInfoService.logout(req.getId());
        return Result.ok();
    }

    @PostMapping("/impersonation-ticket")
    public Result<UserImpersonationTicketResp> impersonationTicket(@RequestBody UserImpersonationTicketReq req)
            throws BizException {
        return Result.ok(userImpersonationService.createTicket(req));
    }

    @PostMapping("/exists")
    public Result<UserExistResp> exists(@RequestBody UserExistReq req) {
        return Result.ok(userInfoService.exists(req.getUserId()));
    }

    @PostMapping("/profile-summaries")
    public Result<List<UserProfileSummaryResp>> profileSummaries(@RequestBody UserProfileSummaryBatchReq req) {
        return Result.ok(userInfoService.profileSummaries(req == null ? List.of() : req.getUserIds()));
    }

    @GetMapping("/vip/config/list")
    public Result<List<VipLevelConfigResp>> vipConfigList(@RequestParam(name = "status", required = false) Integer status)
            throws BizException {
        return Result.ok(vipService.configList(status));
    }

    @PostMapping("/vip/config/save")
    public Result<VipLevelConfigResp> saveVipConfig(@RequestBody VipLevelConfigReq req) throws BizException {
        return Result.ok(vipService.saveConfig(req));
    }

    @PostMapping("/vip/config/delete")
    public Result<Void> deleteVipConfig(@RequestBody IdReq req) throws BizException {
        vipService.deleteConfig(req.getId());
        return Result.ok();
    }

    @PostMapping("/invite/commission/page")
    public Result<Page<InviteCommissionRecordResp>> inviteCommissionPage(
            @RequestBody InviteCommissionRecordQueryReq req) throws BizException {
        return Result.ok(inviteCommissionService.page(req));
    }
}