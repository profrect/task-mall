package com.mall.user.mapper;

import com.mall.user.model.entity.UserInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author GM
* @description 针对表【user_info(用户信息主表)】的数据库操作Mapper
* @createDate 2026-06-22 22:16:24
* @Entity com.mall.user.domain.UserInfo
*/
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}




