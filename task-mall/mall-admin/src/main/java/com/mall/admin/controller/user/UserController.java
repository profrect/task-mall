package com.mall.admin.controller.user;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员相关接口
 */
@RestController
@RequestMapping("/api/admin/user/")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/page")
    public Result<Page<UserInfoVO>> userPageList(UserInfoDTO dto) throws BizException {
        return Result.ok(userService.userPage(dto));
    }
}
