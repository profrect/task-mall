package com.mall.user.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.UserGroupAssignReq;
import com.mall.common.model.dto.req.UserGroupReq;
import com.mall.common.model.dto.resp.UserGroupResp;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.mapper.UserMemberGroupBindMapper;
import com.mall.user.mapper.UserMemberGroupMapper;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.entity.UserMemberGroup;
import com.mall.user.model.entity.UserMemberGroupBind;
import com.mall.user.service.UserMemberGroupService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserMemberGroupServiceImpl implements UserMemberGroupService {

    private static final int STATUS_ENABLED = 1;

    @Resource
    private UserMemberGroupMapper groupMapper;

    @Resource
    private UserMemberGroupBindMapper groupBindMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserGroupResp> list(Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(UserMemberGroup.class)
                .eq(UserMemberGroup::getStatus, status, status != null)
                .orderBy(UserMemberGroup::getSortOrder, true)
                .orderBy(UserMemberGroup::getId, true);
        return groupMapper.selectListByQuery(query).stream()
                .map(this::toResp)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserGroupResp save(UserGroupReq req) throws BizException {
        String groupName = req == null ? null : trim(req.getGroupName());
        Preconditions.needTrue(StringUtils.hasText(groupName), UserRespCodeEnum.USER_GROUP_NAME_EMPTY);

        long sameNameCount = groupMapper.selectCountByQuery(QueryWrapper.create()
                .from(UserMemberGroup.class)
                .eq(UserMemberGroup::getGroupName, groupName)
                .ne(UserMemberGroup::getId, req.getId(), req.getId() != null));
        Preconditions.needTrue(sameNameCount == 0, UserRespCodeEnum.USER_GROUP_NAME_EXISTS, groupName);

        UserMemberGroup group = req.getId() == null ? null : groupMapper.selectOneById(req.getId());
        Preconditions.needTrue(req.getId() == null || group != null, UserRespCodeEnum.USER_GROUP_NOT_FOUND);
        if (group == null) {
            group = new UserMemberGroup();
            group.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
        } else if (req.getStatus() != null) {
            group.setStatus(req.getStatus());
        }
        group.setGroupName(groupName);
        group.setRemark(trim(req.getRemark()));
        group.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        if (group.getId() == null) {
            groupMapper.insert(group);
        } else {
            groupMapper.update(group);
        }
        return toResp(groupMapper.selectOneById(group.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) throws BizException {
        UserMemberGroup group = id == null ? null : groupMapper.selectOneById(id);
        Preconditions.notNull(group, UserRespCodeEnum.USER_GROUP_NOT_FOUND);
        long memberCount = groupBindMapper.selectCountByQuery(QueryWrapper.create()
                .from(UserMemberGroupBind.class)
                .eq(UserMemberGroupBind::getGroupId, id));
        Preconditions.needTrue(memberCount == 0, UserRespCodeEnum.USER_GROUP_IN_USE);
        groupMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(UserGroupAssignReq req) throws BizException {
        List<Long> userIds = normalizeUserIds(req == null ? null : req.getUserIds());
        Preconditions.needTrue(!userIds.isEmpty(), UserRespCodeEnum.USER_BATCH_EMPTY);
        long userCount = userInfoMapper.selectCountByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .in(UserInfo::getUserId, userIds));
        Preconditions.needTrue(userCount == userIds.size(), UserRespCodeEnum.USER_NOT_EXIST);

        Long groupId = req.getGroupId();
        if (groupId != null) {
            UserMemberGroup group = groupMapper.selectOneById(groupId);
            Preconditions.notNull(group, UserRespCodeEnum.USER_GROUP_NOT_FOUND);
        }

        groupBindMapper.deleteByQuery(QueryWrapper.create()
                .from(UserMemberGroupBind.class)
                .in(UserMemberGroupBind::getUserId, userIds));
        if (groupId == null) {
            return;
        }
        for (Long userId : userIds) {
            UserMemberGroupBind bind = new UserMemberGroupBind();
            bind.setUserId(userId);
            bind.setGroupId(groupId);
            groupBindMapper.insert(bind);
        }
    }

    private UserGroupResp toResp(UserMemberGroup group) {
        UserGroupResp resp = new UserGroupResp();
        resp.setId(group.getId());
        resp.setGroupName(group.getGroupName());
        resp.setRemark(group.getRemark());
        resp.setStatus(group.getStatus());
        resp.setSortOrder(group.getSortOrder());
        resp.setCreateTime(group.getCreateTime());
        resp.setUpdateTime(group.getUpdateTime());
        resp.setMemberCount(groupBindMapper.selectCountByQuery(QueryWrapper.create()
                .from(UserMemberGroupBind.class)
                .eq(UserMemberGroupBind::getGroupId, group.getId())));
        return resp;
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
}