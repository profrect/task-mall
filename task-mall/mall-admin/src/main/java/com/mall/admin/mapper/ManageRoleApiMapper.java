package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageRoleApi;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-接口关联关系表(ManageRoleApi)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageRoleApiMapper extends BaseMapper<ManageRoleApi> {
}
