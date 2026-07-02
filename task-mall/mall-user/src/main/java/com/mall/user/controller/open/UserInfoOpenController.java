package com.mall.user.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.user.model.dto.UserInfoDTO;
import com.mall.user.model.dto.VipLevelUpDTO;
import com.mall.user.model.vo.UserDetailVO;
import com.mall.user.model.vo.VipLevelOverviewVO;
import com.mall.user.model.vo.VipUpgradeOrderVO;
import com.mall.user.service.UserInfoService;
import com.mall.user.service.VipService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料接口。所有"本人"动作的 userId 一律由 sa-token 解析（{@link AuthUtils#currentUserId()}），不接受前端传入。
 */
@RestController
@RequestMapping("/api/open/user/")
public class UserInfoOpenController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VipService vipService;

    /**
     * 当前登录用户详情（账号 / 昵称 / VIP / 邀请码 / 邀请人 / 直属团队人数）。
     */
    @GetMapping("/detail")
    public Result<UserDetailVO> currentUser() throws BizException {
        return Result.ok(userInfoService.currentUserDetail(AuthUtils.currentUserId()));
    }

    /**
     * 修改本人资料（仅昵称 / 邮箱）。目标用户取自会话，DTO 不含 userId。
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody @Validated UserInfoDTO userInfoDTO) throws BizException {
        AuthUtils.ensureNotImpersonated();
        userInfoService.updateUserInfo(AuthUtils.currentUserId(), userInfoDTO);
        return Result.ok();
    }

    /**
     * 查询当前用户 VIP 状态与已启用等级配置。
     */
    @GetMapping("/vip-level")
    public Result<VipLevelOverviewVO> vipLevelList() throws BizException {
        return Result.ok(vipService.userOverview(AuthUtils.currentUserId()));
    }

    /**
     * 用户提升 VIP 等级：建单 → 钱包扣款 → 更新用户等级。
     */
    @PostMapping("/vip-level-up")
    public Result<VipUpgradeOrderVO> vipLevelUp(@RequestBody @Validated VipLevelUpDTO levelUpDTO) throws BizException {
        AuthUtils.ensureNotImpersonated();
        return Result.ok(vipService.upgrade(AuthUtils.currentUserId(), levelUpDTO));
    }
}