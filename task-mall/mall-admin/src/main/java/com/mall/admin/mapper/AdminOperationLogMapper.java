package com.mall.admin.mapper;

import com.mall.admin.model.entity.AdminOperationLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 后台操作日志访问层。 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}