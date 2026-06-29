package com.mall.user.service;

import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.user.model.entity.UserInfo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
* @author GM
* @description 针对表【user_info(用户信息主表)】的数据库操作Service
* @createDate 2026-06-22 22:16:24
*/
public interface UserInfoService extends IService<UserInfo> {

    Page<UserResp> pageList(UserReq req);
}
