package com.mall.admin.service.sys.impl;

import cn.hutool.core.collection.CollUtil;
import com.mall.admin.cons.AdminConst;
import com.mall.admin.converter.ManageMenuInfoConverter;
import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.ManageMenuInfoMapper;
import com.mall.admin.mapper.ManageRoleMenuMapper;
import com.mall.admin.model.dto.ManageMenuInfoDTO;
import com.mall.admin.model.entity.ManageMenuInfo;
import com.mall.admin.model.entity.ManageRoleMenu;
import com.mall.admin.model.vo.MenuSampleVO;
import com.mall.admin.model.vo.MenuTagVO;
import com.mall.admin.model.vo.MenuTreeVO;
import com.mall.admin.service.sys.ManageMenuInfoService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.enums.CommonStatusEnum;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.mall.admin.model.entity.table.ManageMenuInfoTableDef.MANAGE_MENU_INFO;
import static com.mall.admin.model.entity.table.ManageRoleMenuTableDef.MANAGE_ROLE_MENU;

/**
 * 系统菜单资源信息表(ManageMenuInfo)表服务接口实现类
 *
 * @author gmxu
 */
@Service
public class ManageMenuInfoServiceImpl extends ServiceImpl<ManageMenuInfoMapper, ManageMenuInfo>
        implements ManageMenuInfoService {

    @Resource
    private ManageMenuInfoConverter manageMenuInfoConverter;

    @Resource
    private ManageRoleMenuMapper manageRoleMenuMapper;

    @Override
    public void add(ManageMenuInfoDTO dto) {
        // 查询排序编号
        QueryWrapper queryWrapper = QueryWrapper.create().from(ManageMenuInfo.class)
                .eq(ManageMenuInfo::getPno, dto.getPno());
        long count = getMapper().selectCountByQuery(queryWrapper);
        dto.setOrder((int) count);

        // 查询菜单级别
        if (dto.getPno() == 0) {
            dto.setMenuLevel(1);
        } else {
            ManageMenuInfo po = getMapper().selectOneById(dto.getPno());
            dto.setMenuLevel(po.getMenuLevel() + 1);
        }

        ManageMenuInfo menuInfoPo = manageMenuInfoConverter.dtoToPo(dto);
        save(menuInfoPo);
    }

    @Override
    public void update(ManageMenuInfoDTO dto) throws BizException {
        Long id = dto.getId();
        ManageMenuInfo menuPo = this.getById(id);
        Preconditions.notNull(menuPo, AdminRespCodeEnum.DATA_NOT_FOUND, id);

        menuPo = manageMenuInfoConverter.dtoToPo(dto);
        // menuKey不允许修改
        menuPo.setMenuKey(null);
        this.updateById(menuPo, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        ManageMenuInfo menuPo = this.getById(id);
        Preconditions.notNull(menuPo, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        // 删除菜单信息
        this.removeById(id);

        // 删除与菜单关联的信息
        QueryWrapper queryWrapper = QueryWrapper.create().from(ManageRoleMenu.class).eq(ManageRoleMenu::getMenuKey, menuPo.getMenuKey());
        manageRoleMenuMapper.deleteByQuery(queryWrapper);
    }

    @Override
    public List<MenuSampleVO> queryList() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ManageMenuInfo.class)
                .eq(ManageMenuInfo::getStatus, CommonStatusEnum.STATUS_YES.getCode())
                .orderBy(ManageMenuInfo::getOrder, true);

        List<ManageMenuInfo> poList;
        // 获取登录用户信息
        UserContext.User user = UserContext.current();
        if (!user.superPermission()) {
            // 非超级权限，则查询对应的权限的菜单
            Collection<String> menuKeys = user.roleMenus();
            if (CollUtil.isNotEmpty(menuKeys)) {
                queryWrapper.in(ManageMenuInfo::getMenuKey, menuKeys);
            }
        }
        poList = getMapper().selectListByQuery(queryWrapper);
        return doGeneralMenuList(0L, poList);
    }

    @Override
    public List<MenuTreeVO> queryTreeList(ManageMenuInfoDTO dto) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ManageMenuInfo.class)
                .eq(ManageMenuInfo::getStatus, CommonStatusEnum.STATUS_YES.getCode())
                .eq(ManageMenuInfo::getName, dto.getName(), StringUtils.hasText(dto.getName()))
                .orderBy(ManageMenuInfo::getOrder, true);
        List<ManageMenuInfo> poList = getMapper().selectListByQuery(queryWrapper);
        return doGeneralMenuTreeList(poList);
    }

    @Override
    public MenuTagVO queryAllMenuTree() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ManageMenuInfo.class)
                .eq(ManageMenuInfo::getStatus, AdminConst.NUM_1)
                .orderBy(ManageMenuInfo::getOrder, true);
        List<ManageMenuInfo> poList = getMapper().selectListByQuery(queryWrapper);

        MenuTagVO vo = new MenuTagVO();
        vo.setSystemMenuTreeList(doGeneralMenuTreeList(poList));
        return vo;
    }

    @Override
    public MenuTagVO queryAllMenuIds(String roleCode) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ManageMenuInfo.class)
                .select(ManageMenuInfo::getId);

        if (!AdminConst.SUPER_USER_ROLE_CODE.equals(roleCode)) {
            // 非超级权限
            queryWrapper.from(MANAGE_MENU_INFO).join(MANAGE_ROLE_MENU)
                    .on(MANAGE_MENU_INFO.MENU_KEY.eq(MANAGE_ROLE_MENU.MENU_KEY))
                    .where(MANAGE_ROLE_MENU.ROLE_CODE.eq(roleCode));
        }

        List<ManageMenuInfo> poList = this.getMapper().selectListByQuery(queryWrapper);
        MenuTagVO vo = new MenuTagVO();
        vo.setSystemMenuIds(poList.stream().map(ManageMenuInfo::getId).toList());
        return vo;
    }

    private List<MenuSampleVO> doGeneralMenuList(Long pno, List<ManageMenuInfo> poList) {
        List<MenuSampleVO> routeList = new LinkedList<>();
        if (CollUtil.isEmpty(poList)) {
            return routeList;
        }
        poList.forEach(m -> {
            if (m.getPno().compareTo(pno) == 0) {
                // 解析当前节点
                MenuSampleVO routeVo = toMenuVo(m);
                // 解析子节点
                List<MenuSampleVO> children = doGeneralMenuList(m.getId(), poList);
                routeVo.setChildren(Collections.emptyList());
                if (!ObjectUtils.isEmpty(children)) {
                    routeVo.setChildren(children);
                }
                routeList.add(routeVo);
            }
        });
        return routeList;
    }

    private MenuSampleVO toMenuVo(ManageMenuInfo route) {
        MenuSampleVO routeVo = new MenuSampleVO();
        routeVo.setName(route.getName());
        routeVo.setPath(route.getPath());
        routeVo.setComponent(route.getComponent());

        MenuSampleVO.Meta meta = new MenuSampleVO.Meta();
        meta.setMenuKey(route.getMenuKey());
        meta.setIcon(route.getIcon());
        meta.setHideInMenu(route.getHideInMenu() == 0);
        meta.setOrder(route.getOrder());
        routeVo.setMeta(meta);
        return routeVo;
    }

    private List<MenuTreeVO> doGeneralMenuTreeList(List<ManageMenuInfo> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 所有菜单信息的编号
        List<Long> allNos = list.stream().map(ManageMenuInfo::getId).toList();
        // 所有菜单的父级编号
        List<Long> allPnos = list.stream().map(ManageMenuInfo::getPno).toList();
        // 获取根节点编号（递归的起点）
        Set<Long> rootNos = allPnos.stream().filter(no -> !allNos.contains(no)).collect(Collectors.toSet());

        // 使用递归函数来构建菜单树
        return rootNos.stream()
                .flatMap(rootId -> buildMenuTree(rootId, list).stream()).toList();
    }

    private List<MenuTreeVO> buildMenuTree(Long pno, List<ManageMenuInfo> poList) {
        if (CollUtil.isEmpty(poList)) {
            return Collections.emptyList();
        }
        return poList.stream().filter(menu -> menu.getPno().equals(pno))
                .map(entity -> {
                    MenuTreeVO menuVO = toTreeVo(entity);
                    menuVO.setChildren(null);
                    List<MenuTreeVO> children = buildMenuTree(entity.getId(), poList);
                    if (CollUtil.isNotEmpty(children)) {
                        menuVO.setChildren(children);
                    }
                    return menuVO;
                }).collect(Collectors.toList());
    }

    private MenuTreeVO toTreeVo(ManageMenuInfo menu) {
        MenuTreeVO treeVo = new MenuTreeVO();
        treeVo.setKey(menu.getId());
        treeVo.setPno(menu.getPno());
        treeVo.setMenuKey(menu.getMenuKey());
        treeVo.setType(menu.getType());
        treeVo.setName(menu.getName());
        treeVo.setPath(menu.getPath());
        treeVo.setComponent(menu.getComponent());
        treeVo.setOrder(menu.getOrder());
        treeVo.setIcon(menu.getIcon());
        treeVo.setHideInMenu(menu.getHideInMenu());
        return treeVo;
    }
}
