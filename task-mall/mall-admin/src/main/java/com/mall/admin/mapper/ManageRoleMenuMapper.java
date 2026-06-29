package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageRoleMenu;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联关系表(ManageRoleMenu)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageRoleMenuMapper extends BaseMapper<ManageRoleMenu> {
}
