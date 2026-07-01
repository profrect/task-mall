package com.mall.user.controller.provider;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.UserExistReq;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.req.UserProfileSummaryBatchReq;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.req.UserStatusUpdateReq;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import com.mall.user.service.InviteCommissionService;
import com.mall.user.service.UserInfoService;
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

    @PostMapping("/user-page")
    public Result<Page<UserResp>> userPage(@RequestBody UserReq req){
        return Result.ok(userInfoService.pageList(req));
    }

    @GetMapping("/stats")
    public Result<UserStatsResp> stats(@RequestParam Long todayStartAt) {
        return Result.ok(userInfoService.stats(todayStartAt));
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody UserStatusUpdateReq req) throws BizException {
        userInfoService.updateStatus(req.getUserId(), req.getStatus());
        return Result.ok();
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
    public Result<List<VipLevelConfigResp>> vipConfigList(@RequestParam(required = false) Integer status)
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