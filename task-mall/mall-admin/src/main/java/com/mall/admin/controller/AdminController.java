package com.mall.admin.controller;

import com.mall.admin.model.dto.AdminLoginDTO;
import com.mall.admin.model.vo.AdminLoginVO;
import com.mall.admin.service.sys.AdminService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/admin")
@Validated
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Validated AdminLoginDTO dto) throws BizException {
        return Result.ok(adminService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        adminService.logout();
        return Result.ok();
    }
}
