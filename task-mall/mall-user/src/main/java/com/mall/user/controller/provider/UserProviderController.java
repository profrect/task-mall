package com.mall.user.controller.provider;

import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider/user/")
public class UserProviderController {

    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/user-page")
    public Result<Page<UserResp>> userPage(UserReq req){
        return Result.ok(userInfoService.pageList(req));
    }
}
