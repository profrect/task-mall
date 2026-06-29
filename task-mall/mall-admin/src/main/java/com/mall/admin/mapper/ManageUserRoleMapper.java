package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageUserRole;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-用户角色关联关系表(ManageUserRole)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageUserRoleMapper extends BaseMapper<ManageUserRole> {
}
