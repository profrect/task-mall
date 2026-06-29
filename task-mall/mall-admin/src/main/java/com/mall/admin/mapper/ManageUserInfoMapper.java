package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageUserInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息表(ManageUserInfo)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageUserInfoMapper extends BaseMapper<ManageUserInfo> {
}
