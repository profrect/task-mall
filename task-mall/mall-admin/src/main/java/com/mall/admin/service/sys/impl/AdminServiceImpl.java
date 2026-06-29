package com.mall.admin.service.sys.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mall.admin.cons.AdminConst;
import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.ManageRoleApiMapper;
import com.mall.admin.mapper.ManageRoleMenuMapper;
import com.mall.admin.mapper.ManageUserInfoMapper;
import com.mall.admin.mapper.ManageUserRoleMapper;
import com.mall.admin.model.dto.AdminLoginDTO;
import com.mall.admin.model.entity.ManageRoleApi;
import com.mall.admin.model.entity.ManageRoleMenu;
import com.mall.admin.model.entity.ManageUserInfo;
import com.mall.admin.model.entity.ManageUserRole;
import com.mall.admin.model.vo.AdminLoginVO;
import com.mall.admin.service.sys.AdminService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private ManageUserInfoMapper manageUserInfoMapper;

    @Resource
    private ManageUserRoleMapper manageUserRoleMapper;

    @Resource
    private ManageRoleApiMapper manageRoleApiMapper;

    @Resource
    private ManageRoleMenuMapper manageRoleMenuMapper;

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) throws BizException {
        // 检查用户名和密码
        ManageUserInfo userInfo = doCheckUserAndPassword(dto.getUsername(), dto.getUserPswd());
        String username = userInfo.getUsername();

        // 查询用户角色，根据用户角色查询所拥有的接口、菜单权限
        UserContext.User user = queryUserPermission(userInfo, username);

        // 登录，并设置会话信息
        StpUtil.login(username);
        String tokenValue = StpUtil.getTokenValueByLoginId(username);
        SaSession tokenSession = StpUtil.getTokenSessionByToken(tokenValue);
        tokenSession.set(username, user);

        // 返回数据
        return AdminLoginVO.builder()
                .username(username)
                .accessToken(tokenValue)
                .expires(StpUtil.getTokenTimeout(tokenValue))
                .realName(userInfo.getRealName())
                .email(userInfo.getEmail())
                .level(userInfo.getLevel()).build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public ManageUserInfo doCheckUserAndPassword(String username, String userPswd) throws BizException {
        QueryWrapper queryWrapper = QueryWrapper.create().from(ManageUserInfo.class)
                .eq(ManageUserInfo::getUsername, username);
        ManageUserInfo userInfoPo = manageUserInfoMapper.selectOneByQuery(queryWrapper);
        Preconditions.notNull(userInfoPo, AdminRespCodeEnum.USER_NOT_EXIST, username);

        // 如果加密，则需要先解密 密码
        boolean result = BCrypt.checkpw(userPswd, userInfoPo.getPasswordHash());
        Preconditions.needTrue(result, AdminRespCodeEnum.PASSWORD_ERROR);
        return userInfoPo;
    }

    @SuppressWarnings("all")
    private UserContext.User queryUserPermission(ManageUserInfo userInfo, String username) {
        boolean superPermission = false;
        Set<String> roleMenuSet = Collections.emptySet();
        Set<String> roleApiSet = Collections.emptySet();

        // 获取用户角色，通过用户角色去查询对应的权限
        QueryWrapper roleQueryWrapper = QueryWrapper.create().from(ManageUserRole.class)
                .select(ManageUserRole::getRoleCode)
                .eq(ManageUserRole::getUsername, userInfo.getUsername());
        List<String> roleCodes = manageUserRoleMapper.selectListByQueryAs(roleQueryWrapper, String.class);
        if (CollUtil.isNotEmpty(roleCodes)) {
            // 是否拥有超级管理员权限
            superPermission = roleCodes.contains(AdminConst.SUPER_USER_ROLE_CODE);

            // 查询菜单权限
            QueryWrapper menuQueryWrapper = QueryWrapper.create().from(ManageRoleMenu.class)
                    .select(ManageRoleMenu::getMenuKey)
                    .in(ManageRoleMenu::getRoleCode, roleCodes);
            List<String> roleMenuList = manageRoleMenuMapper.selectListByQueryAs(menuQueryWrapper, String.class);
            roleMenuSet = new HashSet<>(roleMenuList);

            // 查询接口权限
            QueryWrapper apiQueryWrapper = QueryWrapper.create().from(ManageRoleApi.class)
                    .select(ManageRoleApi::getApiCode)
                    .in(ManageRoleApi::getRoleCode, roleCodes);
            List<String> roleApiList = manageRoleApiMapper.selectListByQueryAs(apiQueryWrapper, String.class);
            roleApiSet = new HashSet<>(roleApiList);
        }
        return UserContext.User.builder().username(username).superPermission(superPermission)
                .roles(roleCodes).roleMenus(roleMenuSet).roleApis(roleApiSet).build();
    }
}
