package com.mall.admin.service.sys.impl;

import cn.hutool.core.collection.CollUtil;
import com.mall.admin.cons.AdminConst;
import com.mall.admin.converter.ManageRoleInfoConverter;
import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.*;
import com.mall.admin.model.dto.ManageRoleInfoDTO;
import com.mall.admin.model.entity.*;
import com.mall.admin.model.vo.ManageRoleInfoVO;
import com.mall.admin.service.sys.ManageRoleInfoService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.model.Option;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 角色信息表(ManageRoleInfo)表服务接口实现类
 *
 * @author gmxu
 */
@Service
public class ManageRoleInfoServiceImpl extends ServiceImpl<ManageRoleInfoMapper, ManageRoleInfo>
        implements ManageRoleInfoService {

    @Resource
    private ManageRoleInfoConverter manageRoleInfoConverter;

    @Resource
    private ManageUserRoleMapper manageUserRoleMapper;

    @Resource
    private ManageRoleMenuMapper manageRoleMenuMapper;

    @Resource
    private ManageRoleApiMapper manageRoleApiMapper;

    @Resource
    private ManageMenuInfoMapper manageMenuInfoMapper;

    @Resource
    private ManageApiInfoMapper manageApiInfoMapper;

    @Override
    public Page<ManageRoleInfoVO> pageList(ManageRoleInfoDTO dto) {
        QueryWrapper queryWrapper = QueryWrapper.create().select().from(ManageRoleInfo.class)
                .eq(ManageRoleInfo::getRoleCode, dto.getRoleCode(), StringUtils.hasText(dto.getRoleCode()))
                .eq(ManageRoleInfo::getRoleName, dto.getRoleName(), StringUtils.hasText(dto.getRoleName()))
                .ne(ManageRoleInfo::getRoleCode, AdminConst.SUPER_USER_ROLE_CODE);
        Page<ManageRoleInfoVO> pageResult = this.getMapper().paginateWithRelationsAs(
                Page.of(dto.getPageNumber(), dto.getPageSize()), queryWrapper, ManageRoleInfoVO.class);
        List<ManageRoleInfoVO> records = pageResult.getRecords();
        records.forEach(d -> {
            List<Long> menuIds = d.getMenuList().stream().map(ManageMenuInfo::getId).toList();
            d.setMenuIds(menuIds);

            List<Long> apiIds = d.getApiList().stream().map(ManageApiInfo::getId).toList();
            d.setApiIds(apiIds);
        });
        return pageResult;
    }

    @Override
    public void add(ManageRoleInfoDTO dto) throws BizException {
        ManageRoleInfo roleInfo = queryRoleByCode(dto.getRoleCode());
        Preconditions.needTrue(Objects.isNull(roleInfo), AdminRespCodeEnum.ROLE_CODE_REPEAT, dto.getRoleCode());

        ManageRoleInfo addPo = manageRoleInfoConverter.dtoToPo(dto);
        addPo.setLevel(2);
        save(addPo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ManageRoleInfoDTO dto) throws BizException {
        ManageRoleInfo roleInfo = getById(dto.getId());
        Preconditions.notNull(roleInfo, AdminRespCodeEnum.DATA_NOT_FOUND, dto.getId());

        // 修改角色信息
        ManageRoleInfo updatePo = manageRoleInfoConverter.dtoToPo(dto);
        updatePo.setRoleCode(null);
        updateById(updatePo, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        ManageRoleInfo roleInfo = getById(id);
        Preconditions.notNull(roleInfo, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        String roleCode = roleInfo.getRoleCode();

        removeById(id);
        manageUserRoleMapper.deleteByQuery(QueryWrapper.create().from(ManageUserRole.class).eq(ManageUserRole::getRoleCode, roleCode));
        manageRoleMenuMapper.deleteByQuery(QueryWrapper.create().from(ManageRoleMenu.class).eq(ManageRoleMenu::getRoleCode, roleCode));
        manageRoleApiMapper.deleteByQuery(QueryWrapper.create().from(ManageRoleApi.class).eq(ManageRoleApi::getRoleCode, roleCode));
    }

    @Override
    public List<Option<String, String>> optionList() {
        List<ManageRoleInfo> poList = this.mapper.selectListByQuery(
                QueryWrapper.create()
                        .select(ManageRoleInfo::getRoleCode, ManageRoleInfo::getRoleName)
                        .from(ManageRoleInfo.class)
                        .ne(ManageRoleInfo::getRoleCode, AdminConst.SUPER_USER_ROLE_CODE)
        );

        List<Option<String, String>> optionList = new ArrayList<>(poList.size());
        poList.forEach(p -> optionList.add(new Option<>(p.getRoleName(), p.getRoleCode())));
        return optionList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRoleMenu(String roleCode, List<Long> idList) throws BizException {
        ManageRoleInfo roleInfo = queryRoleByCode(roleCode);
        Preconditions.notNull(roleInfo, AdminRespCodeEnum.ROLE_NOT_EXIST, roleCode);

        // 删除角色-菜单 关联关系
        manageRoleMenuMapper.deleteByQuery(QueryWrapper.create().from(ManageRoleMenu.class).eq(ManageRoleMenu::getRoleCode, roleCode));
        if(CollUtil.isEmpty(idList)) {
            return;
        }

        // 重新建立角色-菜单 关联关系
        List<ManageMenuInfo> menuInfoList = manageMenuInfoMapper.selectListByQuery(QueryWrapper.create()
                .from(ManageMenuInfo.class).in(ManageMenuInfo::getId, idList));
        if(CollUtil.isEmpty(menuInfoList)) {
            return;
        }
        List<ManageRoleMenu> roleMenuList = new ArrayList<>(menuInfoList.size());
        menuInfoList.forEach(m -> {
            ManageRoleMenu roleMenu = new ManageRoleMenu();
            roleMenu.setRoleCode(roleCode);
            roleMenu.setMenuKey(m.getMenuKey());
            roleMenuList.add(roleMenu);
        });
        manageRoleMenuMapper.insertBatch(roleMenuList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRoleApi(String roleCode, List<Long> idList) throws BizException {
        ManageRoleInfo roleInfo = queryRoleByCode(roleCode);
        Preconditions.notNull(roleInfo, AdminRespCodeEnum.ROLE_NOT_EXIST, roleCode);

        // 删除角色-api 关联关系
        manageRoleApiMapper.deleteByQuery(QueryWrapper.create().from(ManageRoleApi.class).eq(ManageRoleApi::getRoleCode, roleCode));
        if(CollUtil.isEmpty(idList)) {
            return;
        }

        // 重新建立角色-api 关联关系
        List<ManageApiInfo> apiInfoList = manageApiInfoMapper.selectListByQuery(QueryWrapper.create()
                .from(ManageApiInfo.class).in(ManageApiInfo::getId, idList));
        if(CollUtil.isEmpty(apiInfoList)) {
            return;
        }
        List<ManageRoleApi> roleApiList = new ArrayList<>(apiInfoList.size());
        apiInfoList.forEach(a -> {
            ManageRoleApi roleApi = new ManageRoleApi();
            roleApi.setRoleCode(roleCode);
            roleApi.setApiCode(a.getApiCode());
            roleApiList.add(roleApi);
        });
        manageRoleApiMapper.insertBatch(roleApiList);
    }

    private ManageRoleInfo queryRoleByCode(String roleCode) {
        return this.mapper.selectOneByQuery(QueryWrapper.create().select().from(ManageRoleInfo.class)
                .eq(ManageRoleInfo::getRoleCode, roleCode));
    }
}
