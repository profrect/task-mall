package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageRoleInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色信息表(ManageRoleInfo)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageRoleInfoMapper extends BaseMapper<ManageRoleInfo> {
}
