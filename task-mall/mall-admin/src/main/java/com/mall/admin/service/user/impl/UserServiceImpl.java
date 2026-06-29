package com.mall.admin.service.user.impl;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private ApiRestClient apiRestClient;

    @Override
    public Page<UserInfoVO> userPage(UserInfoDTO dto) throws BizException {
        // TODO 查询用户中心
        UserReq userReq = new UserReq();
        Page<?> userResp = apiRestClient.post("http://127.0.0.1:12000/api/provider/user/user-page", userReq, Page.class);

        log.info("-------->> {}", userResp);
        // TODO 再根据用户信息查询钱包中心

        return null;
    }
}
