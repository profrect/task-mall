package com.mall.admin.controller;

import com.mall.admin.model.dto.ManageUserInfoDTO;
import com.mall.admin.model.dto.ManageUserPageDTO;
import com.mall.admin.model.dto.ManageUserPswdDTO;
import com.mall.admin.model.vo.ManageUserInfoVO;
import com.mall.admin.service.sys.ManageUserInfoService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.core.valid.ValidGroups;
import com.mybatisflex.core.paginate.Page;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息表 控制层
 *
 * @author xiaoc
 */
@RestController
@RequestMapping("/api/sys/manage/user")
public class ManageUserInfoController {

    @Resource
    private ManageUserInfoService manageUserInfoService;

    @GetMapping("/page-list")
    public Result<Page<ManageUserInfoVO>> page(ManageUserPageDTO dto) {
        return Result.ok(manageUserInfoService.pageList(dto));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Validated({ValidGroups.Insert.class}) ManageUserInfoDTO dto) throws BizException {
        manageUserInfoService.add(dto);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> remove(@PathVariable("id") @NotNull(message = "用户id不能为空") Long id) throws BizException {
        manageUserInfoService.delete(id);
        return Result.ok();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Validated({ValidGroups.Update.class}) ManageUserInfoDTO dto) throws BizException {
        manageUserInfoService.update(dto);
        return Result.ok();
    }

    @GetMapping("/user-detail")
    public Result<ManageUserInfoVO> userDetail() throws BizException {
        return Result.ok(manageUserInfoService.userDetail());
    }

    @PostMapping("/update-pswd")
    public Result<Void> updatePswd(@RequestBody @Validated ManageUserPswdDTO dto) throws BizException {
        manageUserInfoService.updatePswd(dto);
        return Result.ok();
    }

    @PostMapping("/reset-pswd/{username}")
    public Result<Void> resetPswd(@PathVariable("username") @NotNull(message = "用户名不能为空") String username) throws BizException {
        manageUserInfoService.resetPswd(username);
        return Result.ok();
    }
}
