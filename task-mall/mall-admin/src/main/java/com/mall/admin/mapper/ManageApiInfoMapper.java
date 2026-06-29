package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageApiInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统接口资源信息表(ManageApiInfo)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageApiInfoMapper extends BaseMapper<ManageApiInfo> {
}
