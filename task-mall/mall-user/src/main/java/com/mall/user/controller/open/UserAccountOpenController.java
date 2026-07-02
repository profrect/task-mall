package com.mall.user.controller.open;

import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.UserImpersonationExchangeReq;
import com.mall.user.model.dto.UserChangePwdDTO;
import com.mall.user.model.dto.UserLoginDTO;
import com.mall.user.model.dto.UserRegisterDTO;
import com.mall.user.model.vo.UserLoginVO;
import com.mall.user.service.UserAccountService;
import com.mall.user.service.UserImpersonationService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/open/user/account/")
public class UserAccountOpenController {

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserImpersonationService userImpersonationService;

    @PostMapping("/register")
    public Result<Void> userRegister(@RequestBody @Validated UserRegisterDTO registerDTO) throws BizException {
        userAccountService.userRegister(registerDTO);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody @Validated UserLoginDTO loginDTO) throws BizException {
        return Result.ok(userAccountService.login(loginDTO));
    }

    @PostMapping("/impersonation-exchange")
    public Result<UserLoginVO> impersonationExchange(@RequestBody @Validated UserImpersonationExchangeReq req)
            throws BizException {
        return Result.ok(userImpersonationService.exchange(req));
    }

    @PutMapping("/change-pwd")
    public Result<Void> passwordChange(@RequestBody @Validated UserChangePwdDTO changePwdDTO) throws BizException {
        AuthUtils.ensureNotImpersonated();
        userAccountService.passwordChange(changePwdDTO);
        return Result.ok();
    }
}
