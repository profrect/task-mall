package com.mall.admin.service.sys.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONUtil;
import com.mall.admin.cons.AdminConst;
import com.mall.admin.converter.ManageUserInfoConverter;
import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.ManageRoleInfoMapper;
import com.mall.admin.mapper.ManageUserInfoMapper;
import com.mall.admin.mapper.ManageUserRoleMapper;
import com.mall.admin.model.dto.ManageUserInfoDTO;
import com.mall.admin.model.dto.ManageUserPageDTO;
import com.mall.admin.model.dto.ManageUserPswdDTO;
import com.mall.admin.model.entity.ManageRoleInfo;
import com.mall.admin.model.entity.ManageUserInfo;
import com.mall.admin.model.entity.ManageUserRole;
import com.mall.admin.model.vo.ManageUserInfoVO;
import com.mall.admin.service.sys.AdminService;
import com.mall.admin.service.sys.ManageUserInfoService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户信息表(ManageUserInfo)表服务接口实现类
 *
 * @author gmxu
 */
@Slf4j
@Service
public class ManageUserInfoServiceImpl extends ServiceImpl<ManageUserInfoMapper, ManageUserInfo>
        implements ManageUserInfoService {

    private final static String ADMIN = "admin";

    @Resource
    private ManageUserInfoConverter manageUserInfoConverter;

    @Resource
    private ManageUserRoleMapper manageUserRoleMapper;

    @Resource
    private ManageRoleInfoMapper manageRoleInfoMapper;

    @Resource
    private AdminService adminService;

    @SuppressWarnings("all")
    @Override
    public Page<ManageUserInfoVO> pageList(ManageUserPageDTO dto) {
        Page<ManageUserInfoVO> page = Page.of(dto.getPageNumber(), dto.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create().from(ManageUserInfo.class)
                .eq(ManageUserInfo::getUsername, dto.getUsername(), StringUtils.hasText(dto.getUsername()))
                .ne(ManageUserInfo::getUsername, ADMIN);

        // 如果角色条件不为空
        if (StringUtils.hasText(dto.getRoleCode())) {
            List<String> userNames = manageUserRoleMapper.selectListByQueryAs(QueryWrapper.create()
                    .from(ManageUserRole.class)
                    .select(ManageUserRole::getUsername)
                    .eq(ManageUserRole::getRoleCode, dto.getRoleCode()), String.class);
            if (CollUtil.isEmpty(userNames)) {
                page.setTotalPage(0L);
                page.setTotalRow(0L);
                return page;
            }
            queryWrapper.in(ManageUserInfo::getUsername, userNames);
        }

        // 查询用户信息，角色等信息
        Page<ManageUserInfoVO> pageResult = this.getMapper().paginateWithRelationsAs(page, queryWrapper, ManageUserInfoVO.class);
        List<ManageUserInfoVO> records = pageResult.getRecords();
        records.forEach(d -> {
            List<ManageRoleInfo> roleList = d.getRoleList();
            d.setRoles(roleList.stream().map(ManageRoleInfo::getRoleName).collect(Collectors.joining(AdminConst.SYMBOL_COMMA)));
        });
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ManageUserInfoDTO dto) throws BizException {
        // 检查用户名是否重名
        final String userName = dto.getUsername();
        ManageUserInfo userInfoPo = queryByName(userName);
        Preconditions.needTrue(Objects.isNull(userInfoPo), AdminRespCodeEnum.USER_NAME_REPEAT, userName);

        // 保存用户信息
        saveUserInfo(dto);

        // 查询用户角色信息
        List<ManageRoleInfo> roleList = queryRoleByCode(dto.getRoleCodeList());
        if (CollUtil.isNotEmpty(roleList)) {
            // 查询用户角色信息
            List<ManageUserRole> userRolePoList = new ArrayList<>(roleList.size());
            roleList.forEach(g -> {
                ManageUserRole userRole = new ManageUserRole();
                userRole.setUsername(userName);
                userRole.setRoleCode(g.getRoleCode());
                userRolePoList.add(userRole);
            });
            manageUserRoleMapper.insertBatch(userRolePoList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ManageUserInfoDTO dto) throws BizException {
        // 查询用户信息
        Long id = dto.getId();
        ManageUserInfo userInfo = getById(id);
        Preconditions.notNull(userInfo, AdminRespCodeEnum.DATA_NOT_FOUND, id);

        // 修改用户数据
        ManageUserInfo updatePo = manageUserInfoConverter.dtoToPo(dto);
        updatePo.setUsername(null);
        updatePo.setPasswordHash(null);
        updateById(updatePo, true);

        // 更新用户角色信息
        List<ManageRoleInfo> roleList = queryRoleByCode(dto.getRoleCodeList());
        if(CollUtil.isNotEmpty(roleList)) {
            QueryWrapper queryWrapper = QueryWrapper.create().from(ManageUserRole.class)
                    .eq(ManageUserRole::getUsername, userInfo.getUsername());
            manageUserRoleMapper.deleteByQuery(queryWrapper);

            // 重新建立关联关系
            List<ManageUserRole> userRolePoList = new ArrayList<>(roleList.size());
            roleList.forEach(r -> {
                ManageUserRole userRole = new ManageUserRole();
                userRole.setUsername(userInfo.getUsername());
                userRole.setRoleCode(r.getRoleCode());
                userRolePoList.add(userRole);
            });
            manageUserRoleMapper.insertBatch(userRolePoList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        ManageUserInfo userInfo = this.getById(id);
        Preconditions.notNull(userInfo, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        // 删除用户信息
        removeById(id);
        // 删除用户角色关联信息
        manageUserRoleMapper.deleteByQuery(QueryWrapper.create().from(ManageUserRole.class)
                .eq(ManageUserRole::getUsername, userInfo.getUsername()));
    }

    @Override
    public ManageUserInfoVO userDetail() throws BizException {
        String username = UserContext.current().username();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ManageUserInfo.class).eq(ManageUserInfo::getUsername, username);
        ManageUserInfoVO userInfoVo = this.getMapper().selectOneByQueryAs(queryWrapper, ManageUserInfoVO.class);
        if (ObjectUtil.isNull(userInfoVo)) {
            StpUtil.logout();
            throw new BizException(AdminRespCodeEnum.USER_NOT_EXIST, null);
        }

        // 查询用户组
        List<String> roleCodes = manageRoleInfoMapper.selectListByQueryAs(
                QueryWrapper.create().select(ManageRoleInfo::getRoleName)
                        .from(ManageRoleInfo.class)
                        .leftJoin(ManageUserRole.class)
                        .on(ManageRoleInfo::getRoleCode, ManageUserRole::getRoleCode)
                        .where(ManageUserRole::getUsername)
                        .eq(username), String.class);
        userInfoVo.setRoles(JSONUtil.toJsonStr(roleCodes));
        return userInfoVo;
    }

    @Override
    public void updatePswd(ManageUserPswdDTO dto) throws BizException {
        String username = dto.getUsername();
        String oldPswd = dto.getOldPswd();
        // 检查用户名和密码
        ManageUserInfo userInfo = adminService.doCheckUserAndPassword(username, oldPswd);

        ManageUserInfo updatePo = new ManageUserInfo();
        updatePo.setId(userInfo.getId());
        updatePo.setPasswordHash(BCrypt.hashpw(dto.getNewPswd()));
        updateById(updatePo, true);

        // 清空用户的登录信息
        StpUtil.logout(username);
    }

    @Override
    public void resetPswd(String username) throws BizException {
        ManageUserInfo userInfo = getMapper().selectOneByQuery(QueryWrapper.create()
                .from(ManageUserInfo.class).eq(ManageUserInfo::getUsername, username));
        Preconditions.notNull(userInfo, AdminRespCodeEnum.DATA_NOT_FOUND, username);

        ManageUserInfo updatePo = new ManageUserInfo();
        updatePo.setId(userInfo.getId());
        updatePo.setPasswordHash(BCrypt.hashpw(username + "123456"));
        updateById(updatePo, true);
        StpUtil.logout(username);
    }

    private ManageUserInfo queryByName(String userName) {
        return getMapper().selectOneByQuery(QueryWrapper.create()
                .from(ManageUserInfo.class)
                .eq(ManageUserInfo::getUsername, userName));
    }

    private void saveUserInfo(ManageUserInfoDTO dto) throws BizException {
        // 密码处理，如果没有上传密码，则使用默认默认（用户名+123456）
        String password = dto.getPassword();
        if (CharSequenceUtil.isBlank(password)) {
            password = dto.getUsername() + "123456";
        }
        dto.setPassword(BCrypt.hashpw(password));
        dto.setLevel(1);
        ManageUserInfo userInfoPo = manageUserInfoConverter.dtoToPo(dto);
        save(userInfoPo);
    }

    private List<ManageRoleInfo> queryRoleByCode(List<String> roleCodes) {
        if (CollUtil.isEmpty(roleCodes)) {
            return new ArrayList<>();
        }
        return manageRoleInfoMapper.selectListByQuery(
                QueryWrapper.create().from(ManageRoleInfo.class)
                        .in(ManageRoleInfo::getRoleCode, roleCodes));
    }
}
