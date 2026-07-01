package com.mall.user.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mall.user.model.vo.TeamMembersVo;
import com.mall.user.service.InviteCommissionService;
import com.mall.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 团队接口。团队归属以当前登录用户为根（inviter = 本人），userId 由会话解析，不接受前端传入。
 */
@RestController
@RequestMapping("/api/open/user/team/")
public class UserTeamOpenController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private InviteCommissionService inviteCommissionService;

    /**
     * 本人直属团队成员分页（昵称 / VIP / 加入时间）。分页参数 pageNumber、pageSize 走 query，缺省 1/10。
     */
    @GetMapping("/members")
    public Result<Page<TeamMembersVo>> teamMembers(BasePageDTO page) throws BizException {
        return Result.ok(userInfoService.teamMembers(AuthUtils.currentUserId(), page));
    }

    /**
     * 本人作为邀请人的返佣明细。只展示真实结算记录；无记录时返回空分页。
     */
    @GetMapping("/earnings")
    public Result<Page<InviteCommissionRecordResp>> teamEarnings(BasePageDTO page) throws BizException {
        return Result.ok(inviteCommissionService.userPage(AuthUtils.currentUserId(), page));
    }
}