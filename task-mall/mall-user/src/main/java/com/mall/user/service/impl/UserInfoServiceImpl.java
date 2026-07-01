package com.mall.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserAccountMapper;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.model.dto.UserInfoDTO;
import com.mall.user.model.entity.UserAccount;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.vo.TeamMembersVo;
import com.mall.user.model.vo.UserDetailVO;
import com.mall.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户资料域实现。读写均限定在 mall-user 本地表（user_info / user_account），不产生跨服务调用。
 * <p>
 * 写操作只落用户可自助变更的字段；身份字段（userId）与受控字段（vipLevel/status/inviteCode/inviter）一律不接受前端改动。
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_FROZEN = 2;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Override
    public Page<UserResp> pageList(UserReq req) {
        long pageNumber = req != null && req.getPageNumber() > 0 ? req.getPageNumber() : 1;
        long pageSize = req != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

        Page<UserInfo> source = getMapper().paginate(pageNumber, pageSize, QueryWrapper.create()
                .from(UserInfo.class)
                .orderBy(UserInfo::getId, false));
        List<UserInfo> users = source.getRecords();
        if (users.isEmpty()) {
            return new Page<>(List.of(), source.getPageNumber(), source.getPageSize(), source.getTotalRow());
        }

        List<Long> userIds = users.stream().map(UserInfo::getUserId).toList();
        Map<Long, UserAccount> accountMap = userAccountMapper.selectListByQuery(QueryWrapper.create()
                        .from(UserAccount.class)
                        .in(UserAccount::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(UserAccount::getUserId, Function.identity(), (a, b) -> a));

        List<Long> inviterIds = users.stream()
                .map(UserInfo::getInviter)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Long, UserAccount> inviterAccountMap = inviterIds.isEmpty()
                ? Map.of()
                : userAccountMapper.selectListByQuery(QueryWrapper.create()
                        .from(UserAccount.class)
                        .in(UserAccount::getUserId, inviterIds))
                .stream()
                .collect(Collectors.toMap(UserAccount::getUserId, Function.identity(), (a, b) -> a));

        List<UserResp> rows = users.stream()
                .map(info -> toUserResp(info, accountMap.get(info.getUserId()), inviterAccountMap.get(info.getInviter())))
                .toList();
        return new Page<>(rows, source.getPageNumber(), source.getPageSize(), source.getTotalRow());
    }

    @Override
    public UserStatsResp stats(long todayStartAt) {
        UserStatsResp resp = new UserStatsResp();
        resp.setTotalUsers(getMapper().selectCountByQuery(QueryWrapper.create().from(UserInfo.class)));
        resp.setTodayNewUsers(getMapper().selectCountByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .ge(UserInfo::getCreateTime, todayStartAt)));
        return resp;
    }

    @Override
    public UserExistResp exists(Long userId) {
        UserExistResp resp = new UserExistResp();
        resp.setUserId(userId);
        if (userId == null || userId <= 0) {
            resp.setExists(false);
            return resp;
        }
        UserInfo info = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, userId));
        resp.setExists(info != null);
        resp.setStatus(info == null ? null : info.getStatus());
        return resp;
    }

    @Override
    public List<UserProfileSummaryResp> profileSummaries(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<Long> ids = userIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return getMapper().selectListByQuery(QueryWrapper.create()
                        .from(UserInfo.class)
                        .in(UserInfo::getUserId, ids))
                .stream()
                .map(this::toProfileSummary)
                .toList();
    }

    @Override
    public void updateStatus(Long userId, Integer status) throws BizException {
        Preconditions.notNull(userId, UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(status != null && (status == STATUS_NORMAL || status == STATUS_FROZEN),
                UserRespCodeEnum.USER_STATUS_INVALID);
        UserInfo info = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, userId));
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);

        UserInfo update = new UserInfo();
        update.setId(info.getId());
        update.setStatus(status);
        getMapper().update(update);

        if (status == STATUS_FROZEN) {
            StpUtil.logout(userId);
        }
    }

    @Override
    public UserDetailVO currentUserDetail(long userId) throws BizException {
        UserInfo info = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, userId));
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);

        UserAccount account = userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, userId));

        long teamCount = getMapper().selectCountByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getInviter, userId));

        return UserDetailVO.builder()
                .userId(userId)
                .account(account == null ? null : account.getAccount())
                .nickName(info.getNickname())
                .vipLevel(String.valueOf(info.getVipLevel()))
                .inviteCode(info.getInviteCode())
                .inviteUser(resolveInviterName(info.getInviter()))
                .teamMemberNum((int) teamCount)
                .build();
    }

    @Override
    public void updateUserInfo(long userId, UserInfoDTO dto) throws BizException {
        UserInfo info = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, userId));
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);

        // 仅按需更新昵称 / 邮箱；以主键定位，避免误伤其它字段
        UserInfo update = new UserInfo();
        update.setId(info.getId());
        if (StringUtils.hasText(dto.nickname())) {
            update.setNickname(dto.nickname().trim());
        }
        if (StringUtils.hasText(dto.email())) {
            update.setEmail(dto.email().trim());
        }
        getMapper().update(update);
    }

    @Override
    public Page<TeamMembersVo> teamMembers(long userId, BasePageDTO page) {
        long pageNumber = page != null && page.getPageNumber() > 0 ? page.getPageNumber() : 1;
        long pageSize = page != null && page.getPageSize() > 0 ? page.getPageSize() : 10;

        Page<UserInfo> source = getMapper().paginate(pageNumber, pageSize, QueryWrapper.create()
                .from(UserInfo.class)
                .eq(UserInfo::getInviter, userId)
                .orderBy(UserInfo::getId, false));

        List<TeamMembersVo> rows = source.getRecords().stream()
                .map(m -> TeamMembersVo.builder()
                        .nickname(m.getNickname())
                        .vipLevel(String.valueOf(m.getVipLevel()))
                        .invitateTime(m.getCreateTime())
                        .build())
                .toList();
        return new Page<>(rows, source.getPageNumber(), source.getPageSize(), source.getTotalRow());
    }

    /** 邀请人昵称（无邀请人或邀请人缺失时返回 null）。 */
    private String resolveInviterName(Long inviterUserId) {
        if (inviterUserId == null) {
            return null;
        }
        UserInfo inviter = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, inviterUserId));
        return inviter == null ? null : inviter.getNickname();
    }

    private UserResp toUserResp(UserInfo info, UserAccount account, UserAccount inviterAccount) {
        UserResp resp = new UserResp();
        resp.setUserId(info.getUserId());
        resp.setUserName(account == null ? null : account.getAccount());
        resp.setVipLevel(info.getVipLevel());
        resp.setInviteCode(info.getInviteCode());
        resp.setStatus(info.getStatus());
        resp.setParentUserName(inviterAccount == null ? null : inviterAccount.getAccount());
        resp.setRegisterTime(info.getCreateTime());
        return resp;
    }

    private UserProfileSummaryResp toProfileSummary(UserInfo info) {
        UserProfileSummaryResp resp = new UserProfileSummaryResp();
        resp.setUserId(info.getUserId());
        resp.setDisplayName(displayName(info));
        resp.setVipLevel(info.getVipLevel());
        resp.setStatus(info.getStatus());
        return resp;
    }

    private String displayName(UserInfo info) {
        if (info == null) {
            return null;
        }
        if (StringUtils.hasText(info.getNickname())) {
            return info.getNickname();
        }
        return info.getUserId() == null ? null : "User " + info.getUserId();
    }
}