package com.mall.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.UserAdminSaveReq;
import com.mall.common.model.dto.req.UserLineChangeReq;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.model.dto.resp.UserLineNodeResp;
import com.mall.common.model.dto.resp.UserLineageResp;
import com.mall.common.model.dto.resp.UserProfileSummaryResp;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.UserStatsResp;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserAccountMapper;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.mapper.UserMemberGroupBindMapper;
import com.mall.user.mapper.UserMemberGroupMapper;
import com.mall.user.model.dto.UserInfoDTO;
import com.mall.user.model.entity.UserAccount;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.entity.UserMemberGroup;
import com.mall.user.model.entity.UserMemberGroupBind;
import com.mall.user.model.vo.TeamMembersVo;
import com.mall.user.model.vo.UserDetailVO;
import com.mall.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final int INVITE_CODE_RADIX = 36;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserMemberGroupBindMapper groupBindMapper;

    @Resource
    private UserMemberGroupMapper groupMapper;

    @Override
    public Page<UserResp> pageList(UserReq req) {
        long pageNumber = req != null && req.getPageNumber() > 0 ? req.getPageNumber() : 1;
        long pageSize = req != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

        QueryWrapper query = QueryWrapper.create()
                .from(UserInfo.class);
        if (req != null) {
            query.eq(UserInfo::getUserId, req.getUserId(), req.getUserId() != null && req.getUserId() > 0)
                    .like(UserInfo::getInviteCode, trim(req.getInviteCode()), StringUtils.hasText(trim(req.getInviteCode())))
                    .eq(UserInfo::getStatus, req.getStatus(), req.getStatus() != null)
                    .ge(UserInfo::getCreateTime, req.getRegisterStartTime(), req.getRegisterStartTime() != null)
                    .le(UserInfo::getCreateTime, req.getRegisterEndTime(), req.getRegisterEndTime() != null);
            if (req.getVipLevel() != null && req.getVipLevel() >= 0) {
                query.eq(UserInfo::getVipLevel, req.getVipLevel());
            }
            if (req.getGroupId() != null && req.getGroupId() > 0) {
                List<Long> groupedUserIds = groupBindMapper.selectListByQuery(QueryWrapper.create()
                                .from(UserMemberGroupBind.class)
                                .eq(UserMemberGroupBind::getGroupId, req.getGroupId()))
                        .stream()
                        .map(UserMemberGroupBind::getUserId)
                        .distinct()
                        .toList();
                if (groupedUserIds.isEmpty()) {
                    return new Page<>(List.of(), pageNumber, pageSize, 0);
                }
                query.in(UserInfo::getUserId, groupedUserIds);
            }

            String userName = trim(req.getUserName());
            if (StringUtils.hasText(userName)) {
                List<Long> matchedUserIds = userAccountMapper.selectListByQuery(QueryWrapper.create()
                                .from(UserAccount.class)
                                .like(UserAccount::getAccount, userName))
                        .stream()
                        .map(UserAccount::getUserId)
                        .distinct()
                        .toList();
                if (matchedUserIds.isEmpty()) {
                    return new Page<>(List.of(), pageNumber, pageSize, 0);
                }
                query.in(UserInfo::getUserId, matchedUserIds);
            }
        }

        Page<UserInfo> source = getMapper().paginate(pageNumber, pageSize, query
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

        Map<Long, UserMemberGroupBind> groupBindMap = groupBindMapper.selectListByQuery(QueryWrapper.create()
                        .from(UserMemberGroupBind.class)
                        .in(UserMemberGroupBind::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(UserMemberGroupBind::getUserId, Function.identity(), (a, b) -> a));
        List<Long> groupIds = groupBindMap.values().stream()
                .map(UserMemberGroupBind::getGroupId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Long, UserMemberGroup> groupMap = groupIds.isEmpty()
                ? Map.of()
                : groupMapper.selectListByQuery(QueryWrapper.create()
                        .from(UserMemberGroup.class)
                        .in(UserMemberGroup::getId, groupIds))
                .stream()
                .collect(Collectors.toMap(UserMemberGroup::getId, Function.identity(), (a, b) -> a));

        List<UserResp> rows = users.stream()
                .map(info -> {
                    Long inviterId = info.getInviter();
                    UserAccount inviterAccount = inviterId == null ? null : inviterAccountMap.get(inviterId);
                    UserMemberGroupBind bind = groupBindMap.get(info.getUserId());
                    UserMemberGroup group = bind == null ? null : groupMap.get(bind.getGroupId());
                    return toUserResp(info, accountMap.get(info.getUserId()), inviterAccount, group);
                })
                .toList();
        return new Page<>(rows, source.getPageNumber(), source.getPageSize(), source.getTotalRow());
    }

    @Override
    public UserResp detail(Long userId) throws BizException {
        UserInfo info = findByUserId(userId);
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);
        UserAccount account = userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, userId));
        UserAccount inviterAccount = info.getInviter() == null ? null : userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, info.getInviter()));
        return toUserResp(info, account, inviterAccount, resolveGroup(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResp adminSave(UserAdminSaveReq req) throws BizException {
        Preconditions.needTrue(req != null, UserRespCodeEnum.ACCOUNT_REQUIRED);
        if (req.getUserId() == null) {
            return createByAdmin(req);
        }
        return updateByAdmin(req);
    }

    @Override
    public UserLineageResp lineage(Long userId) throws BizException {
        UserInfo current = findByUserId(userId);
        Preconditions.notNull(current, UserRespCodeEnum.USER_NOT_EXIST);

        List<UserLineNodeResp> ancestors = new java.util.ArrayList<>();
        java.util.Set<Long> visited = new java.util.HashSet<>();
        Long parentId = current.getInviter();
        int parentDepth = -1;
        while (parentId != null && parentId > 0 && visited.add(parentId)) {
            UserInfo parent = findByUserId(parentId);
            if (parent == null) {
                break;
            }
            ancestors.add(toLineNode(parent, "PARENT", parentDepth));
            parentId = parent.getInviter();
            parentDepth--;
        }

        List<UserLineNodeResp> children = getMapper().selectListByQuery(QueryWrapper.create()
                        .from(UserInfo.class)
                        .eq(UserInfo::getInviter, userId)
                        .orderBy(UserInfo::getId, false))
                .stream()
                .map(child -> toLineNode(child, "CHILD", 1))
                .toList();

        UserLineageResp resp = new UserLineageResp();
        resp.setCurrent(toLineNode(current, "SELF", 0));
        resp.setAncestors(ancestors);
        resp.setChildren(children);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeParent(UserLineChangeReq req) throws BizException {
        Preconditions.needTrue(req != null && req.getUserId() != null && req.getUserId() > 0,
                UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(req.getParentUserId() != null && req.getParentUserId() > 0,
                UserRespCodeEnum.USER_PARENT_INVALID);
        UserInfo info = findByUserId(req.getUserId());
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);
        Long parentUserId = normalizeParent(info.getUserId(), req.getParentUserId());

        UserInfo update = new UserInfo();
        update.setId(info.getId());
        update.setInviter(parentUserId);
        getMapper().update(update);
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
        assertValidStatus(status);
        UserInfo info = findByUserId(userId);
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
    public void updateStatusBatch(List<Long> userIds, Integer status) throws BizException {
        assertValidStatus(status);
        List<Long> ids = normalizeUserIds(userIds);
        Preconditions.needTrue(!ids.isEmpty(), UserRespCodeEnum.USER_BATCH_EMPTY);
        long count = getMapper().selectCountByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .in(UserInfo::getUserId, ids));
        Preconditions.needTrue(count == ids.size(), UserRespCodeEnum.USER_NOT_EXIST);
        for (Long userId : ids) {
            updateStatus(userId, status);
        }
    }

    @Override
    public void logout(Long userId) throws BizException {
        UserInfo info = findByUserId(userId);
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);
        StpUtil.logout(userId);
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

    private UserResp createByAdmin(UserAdminSaveReq req) throws BizException {
        String account = trim(req.getUserName());
        Preconditions.needTrue(StringUtils.hasText(account), UserRespCodeEnum.ACCOUNT_REQUIRED);
        Preconditions.needTrue(StringUtils.hasText(req.getPassword()), UserRespCodeEnum.PASSWORD_REQUIRED);
        assertAccountUnique(account, null);
        Integer status = req.getStatus() == null ? STATUS_NORMAL : req.getStatus();
        assertValidStatus(status);
        Integer vipLevel = normalizeVipLevel(req.getVipLevel());
        Long parentUserId = normalizeParent(null, req.getParentUserId());

        long userId = IdUtil.getSnowflakeNextId();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(trim(req.getEmail()));
        userInfo.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname().trim() : account);
        userInfo.setInviteCode(Long.toString(userId, INVITE_CODE_RADIX).toUpperCase());
        userInfo.setVipLevel(vipLevel);
        userInfo.setInviter(parentUserId);
        userInfo.setStatus(status);
        getMapper().insert(userInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setAccount(account);
        userAccount.setPasswordHash(BCrypt.hashpw(req.getPassword()));
        userAccountMapper.insert(userAccount);
        bindGroup(userId, req.getGroupId());
        return detail(userId);
    }

    private UserResp updateByAdmin(UserAdminSaveReq req) throws BizException {
        UserInfo info = findByUserId(req.getUserId());
        Preconditions.notNull(info, UserRespCodeEnum.USER_NOT_EXIST);
        UserAccount account = userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, info.getUserId()));
        Preconditions.notNull(account, UserRespCodeEnum.USER_NOT_EXIST);

        String nextAccount = trim(req.getUserName());
        if (StringUtils.hasText(nextAccount) && !nextAccount.equals(account.getAccount())) {
            assertAccountUnique(nextAccount, info.getUserId());
            UserAccount accountUpdate = new UserAccount();
            accountUpdate.setId(account.getId());
            accountUpdate.setAccount(nextAccount);
            userAccountMapper.update(accountUpdate);
        }
        if (StringUtils.hasText(req.getPassword())) {
            UserAccount passwordUpdate = new UserAccount();
            passwordUpdate.setId(account.getId());
            passwordUpdate.setPasswordHash(BCrypt.hashpw(req.getPassword()));
            userAccountMapper.update(passwordUpdate);
        }

        UserInfo update = new UserInfo();
        update.setId(info.getId());
        if (req.getNickname() != null) {
            update.setNickname(trim(req.getNickname()));
        }
        if (req.getEmail() != null) {
            update.setEmail(trim(req.getEmail()));
        }
        if (req.getVipLevel() != null) {
            update.setVipLevel(normalizeVipLevel(req.getVipLevel()));
        }
        if (req.getStatus() != null) {
            assertValidStatus(req.getStatus());
            update.setStatus(req.getStatus());
        }
        if (req.getParentUserId() != null) {
            update.setInviter(normalizeParent(info.getUserId(), req.getParentUserId()));
        }
        getMapper().update(update);
        bindGroup(info.getUserId(), req.getGroupId());
        if (Integer.valueOf(STATUS_FROZEN).equals(req.getStatus())) {
            StpUtil.logout(info.getUserId());
        }
        return detail(info.getUserId());
    }

    private void bindGroup(Long userId, Long groupId) throws BizException {
        groupBindMapper.deleteByQuery(QueryWrapper.create()
                .from(UserMemberGroupBind.class)
                .eq(UserMemberGroupBind::getUserId, userId));
        if (groupId == null) {
            return;
        }
        UserMemberGroup group = groupMapper.selectOneById(groupId);
        Preconditions.notNull(group, UserRespCodeEnum.USER_GROUP_NOT_FOUND);
        UserMemberGroupBind bind = new UserMemberGroupBind();
        bind.setUserId(userId);
        bind.setGroupId(groupId);
        groupBindMapper.insert(bind);
    }

    private void assertAccountUnique(String account, Long excludeUserId) throws BizException {
        long existsCount = userAccountMapper.selectCountByQuery(QueryWrapper.create()
                .from(UserAccount.class)
                .eq(UserAccount::getAccount, account)
                .ne(UserAccount::getUserId, excludeUserId, excludeUserId != null));
        Preconditions.needTrue(existsCount == 0, UserRespCodeEnum.ACCOUNT_EXISTS, account);
    }

    private Integer normalizeVipLevel(Integer vipLevel) throws BizException {
        int value = vipLevel == null ? 0 : vipLevel;
        Preconditions.needTrue(value >= 0, UserRespCodeEnum.VIP_LEVEL_INVALID);
        return value;
    }

    private Long normalizeParent(Long userId, Long parentUserId) throws BizException {
        if (parentUserId == null || parentUserId <= 0) {
            return null;
        }
        Preconditions.needTrue(userId == null || !parentUserId.equals(userId), UserRespCodeEnum.USER_PARENT_INVALID);
        UserInfo parent = findByUserId(parentUserId);
        Preconditions.notNull(parent, UserRespCodeEnum.USER_PARENT_INVALID);
        if (userId != null) {
            assertNoParentCycle(userId, parentUserId);
        }
        return parentUserId;
    }

    private void assertNoParentCycle(Long userId, Long parentUserId) throws BizException {
        Long cursor = parentUserId;
        java.util.Set<Long> visited = new java.util.HashSet<>();
        while (cursor != null && cursor > 0 && visited.add(cursor)) {
            Preconditions.needTrue(!cursor.equals(userId), UserRespCodeEnum.USER_LINE_CYCLE);
            UserInfo parent = findByUserId(cursor);
            cursor = parent == null ? null : parent.getInviter();
        }
    }

    private UserMemberGroup resolveGroup(Long userId) {
        UserMemberGroupBind bind = groupBindMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserMemberGroupBind.class)
                .eq(UserMemberGroupBind::getUserId, userId));
        return bind == null ? null : groupMapper.selectOneById(bind.getGroupId());
    }

    private UserLineNodeResp toLineNode(UserInfo info, String relation, int depth) {
        UserAccount account = userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, info.getUserId()));
        UserAccount parentAccount = info.getInviter() == null ? null : userAccountMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserAccount.class).eq(UserAccount::getUserId, info.getInviter()));
        UserLineNodeResp node = new UserLineNodeResp();
        node.setUserId(info.getUserId());
        node.setUserName(account == null ? null : account.getAccount());
        node.setNickname(info.getNickname());
        node.setVipLevel(info.getVipLevel());
        node.setStatus(info.getStatus());
        node.setParentUserId(info.getInviter());
        node.setParentUserName(parentAccount == null ? null : parentAccount.getAccount());
        node.setRelation(relation);
        node.setDepth(depth);
        return node;
    }

    /** 邀请人昵称（无邀请人或邀请人缺失时返回 null）。 */
    private String resolveInviterName(Long inviterUserId) {
        if (inviterUserId == null) {
            return null;
        }
        UserInfo inviter = findByUserId(inviterUserId);
        return inviter == null ? null : inviter.getNickname();
    }

    private UserInfo findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return getMapper().selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class).eq(UserInfo::getUserId, userId));
    }

    private void assertValidStatus(Integer status) throws BizException {
        Preconditions.needTrue(status != null && (status == STATUS_NORMAL || status == STATUS_FROZEN),
                UserRespCodeEnum.USER_STATUS_INVALID);
    }

    private List<Long> normalizeUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private UserResp toUserResp(UserInfo info, UserAccount account, UserAccount inviterAccount, UserMemberGroup group) {
        UserResp resp = new UserResp();
        resp.setUserId(info.getUserId());
        resp.setUserName(account == null ? null : account.getAccount());
        resp.setNickname(info.getNickname());
        resp.setEmail(info.getEmail());
        resp.setVipLevel(info.getVipLevel());
        resp.setInviteCode(info.getInviteCode());
        resp.setStatus(info.getStatus());
        resp.setParentUserName(inviterAccount == null ? null : inviterAccount.getAccount());
        resp.setParentUserId(info.getInviter());
        resp.setGroupId(group == null ? null : group.getId());
        resp.setGroupName(group == null ? null : group.getGroupName());
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