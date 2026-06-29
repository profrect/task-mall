package com.mall.user.controller.open;

import com.mall.common.core.result.Result;
import com.mall.user.model.dto.UserChangePwdDTO;
import com.mall.user.model.dto.UserLoginDTO;
import com.mall.user.model.dto.UserRegisterDTO;
import com.mall.user.model.vo.UserLoginVO;
import com.mall.user.service.UserAccountService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/open/user/account/")
public class UserAccountOpenController {

    @Resource
    private UserAccountService userAccountService;

    @PostMapping("/register")
    public Result<Void> userRegister(@RequestBody @Validated UserRegisterDTO registerDTO){
        userAccountService.userRegister(registerDTO);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(UserLoginDTO loginDTO){
        return Result.ok(userAccountService.login(loginDTO));
    }

    @PutMapping("/change-pwd")
    public Result<Void> passwordChange(UserChangePwdDTO changePwdDTO){
        userAccountService.passwordChange(changePwdDTO);
        return Result.ok();
    }
}
