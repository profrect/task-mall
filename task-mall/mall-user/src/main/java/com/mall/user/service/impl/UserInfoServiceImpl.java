package com.mall.user.service.impl;


import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.service.UserInfoService;
import com.mall.user.mapper.UserInfoMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author GM
* @description 针对表【user_info(用户信息主表)】的数据库操作Service实现
* @createDate 2026-06-22 22:16:24
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{

    @Override
    public Page<UserResp> pageList(UserReq req) {
        QueryWrapper queryWrapper = QueryWrapper.create().from(UserInfo.class);
        Page<UserResp> userRespPage = getMapper().paginateAs(req.getPageNumber(), req.getPageSize(), queryWrapper, UserResp.class);

        return userRespPage;
    }
}




