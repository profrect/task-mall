package com.mall.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserAccountMapper;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.model.dto.UserChangePwdDTO;
import com.mall.user.model.dto.UserLoginDTO;
import com.mall.user.model.dto.UserRegisterDTO;
import com.mall.user.model.entity.UserAccount;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.vo.UserLoginVO;
import com.mall.user.service.UserAccountService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final int STATUS_NORMAL = 1;

    /** 邀请码进制：用全局 userId 直接派生，天然唯一、无需额外查重 */
    private static final int INVITE_CODE_RADIX = 36;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userRegister(UserRegisterDTO dto) throws BizException {
        String account = dto.account().trim();

        // 账号唯一性（DB uk_account 兜底，这里做友好提示）
        long existsCount = userAccountMapper.selectCountByQuery(
                QueryWrapper.create().from(UserAccount.class).eq(UserAccount::getAccount, account));
        Preconditions.needTrue(existsCount == 0, UserRespCodeEnum.ACCOUNT_EXISTS, account);

        // 邀请码可选：仅做邀请关系绑定，注册不要求任何实名/KYC
        Long inviterUserId = resolveInviter(dto.inviteCode());

        long userId = IdUtil.getSnowflakeNextId();

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(dto.email());
        userInfo.setNickname(StrUtil.isBlank(dto.nickname()) ? account : dto.nickname());
        userInfo.setInviteCode(Long.toString(userId, INVITE_CODE_RADIX).toUpperCase());
        userInfo.setVipLevel(0);
        userInfo.setInviter(inviterUserId);
        userInfo.setStatus(STATUS_NORMAL);
        userInfoMapper.insert(userInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setAccount(account);
        userAccount.setPasswordHash(BCrypt.hashpw(dto.password()));
        userAccountMapper.insert(userAccount);

        log.info("--->> 用户注册成功 userId={}, account={}", userId, account);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) throws BizException {
        UserAccount account = userAccountMapper.selectOneByQuery(
                QueryWrapper.create().from(UserAccount.class).eq(UserAccount::getAccount, dto.account().trim()));
        // 账号不存在与密码错误返回同一码，避免账号枚举
        Preconditions.needTrue(account != null && BCrypt.checkpw(dto.password(), account.getPasswordHash()),
                UserRespCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);

        UserInfo userInfo = userInfoMapper.selectOneByQuery(
                QueryWrapper.create().from(UserInfo.class).eq(UserInfo::getUserId, account.getUserId()));
        Preconditions.notNull(userInfo, UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(userInfo.getStatus() == null || userInfo.getStatus() == STATUS_NORMAL,
                UserRespCodeEnum.ACCOUNT_FROZEN);

        // loginId = 全局 userId：wallet 等下游服务凭共享 Redis 的 sa-token 解析出 userId
        StpUtil.login(account.getUserId());
        return UserLoginVO.builder()
                .accessToken(StpUtil.getTokenValue())
                .expiresIn(StpUtil.getTokenTimeout())
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passwordChange(UserChangePwdDTO dto) throws BizException {
        long userId = AuthUtils.currentUserId();
        UserAccount account = userAccountMapper.selectOneByQuery(
                QueryWrapper.create().from(UserAccount.class).eq(UserAccount::getUserId, userId));
        Preconditions.notNull(account, UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(BCrypt.checkpw(dto.origPassword(), account.getPasswordHash()),
                UserRespCodeEnum.OLD_PASSWORD_ERROR);

        UserAccount update = new UserAccount();
        update.setId(account.getId());
        update.setPasswordHash(BCrypt.hashpw(dto.newPassword()));
        userAccountMapper.update(update);
    }

    private Long resolveInviter(String inviteCode) throws BizException {
        if (StrUtil.isBlank(inviteCode)) {
            return null;
        }
        UserInfo inviter = userInfoMapper.selectOneByQuery(
                QueryWrapper.create().from(UserInfo.class).eq(UserInfo::getInviteCode, inviteCode.trim()));
        Preconditions.notNull(inviter, UserRespCodeEnum.INVITE_CODE_INVALID);
        return inviter.getUserId();
    }
}