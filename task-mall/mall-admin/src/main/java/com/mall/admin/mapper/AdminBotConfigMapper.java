package com.mall.admin.mapper;

import com.mall.admin.model.entity.AdminBotConfig;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 机器人配置表访问层。 */
@Mapper
public interface AdminBotConfigMapper extends BaseMapper<AdminBotConfig> {
}