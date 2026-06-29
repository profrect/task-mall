package com.mall.admin.controller;

import com.mall.admin.model.dto.ManageRoleInfoDTO;
import com.mall.admin.model.dto.RoleAssignIdsDTO;
import com.mall.admin.model.vo.ManageRoleInfoVO;
import com.mall.admin.service.sys.ManageRoleInfoService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.model.Option;
import com.mall.common.core.result.Result;
import com.mall.common.core.valid.ValidGroups;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色信息表 控制层
 *
 * @author xiaoc
 */
@RestController
@RequestMapping("/api/sys/manage/role")
public class ManageRoleInfoController {

    @Resource
    private ManageRoleInfoService manageRoleInfoService;

    @GetMapping("/page-list")
    public Result<Page<ManageRoleInfoVO>> pageList(ManageRoleInfoDTO dto) {
        return Result.ok(manageRoleInfoService.pageList(dto));
    }

    @GetMapping("/option-list")
    public Result<List<Option<String, String>>> optionList() {
        return Result.ok(manageRoleInfoService.optionList());
    }

    @PostMapping("/add")
    public Result<Void> save(@RequestBody @Validated({ValidGroups.Insert.class}) ManageRoleInfoDTO dto) throws BizException {
        manageRoleInfoService.add(dto);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> remove(@PathVariable("id") @NotNull(message = "角色id不能为空") Long id) throws BizException {
        manageRoleInfoService.delete(id);
        return Result.ok();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody @Validated({ValidGroups.Update.class}) ManageRoleInfoDTO dto) throws BizException {
        manageRoleInfoService.update(dto);
        return Result.ok();
    }

    @PostMapping("/assign-menu")
    public Result<Void> assignRoleMenu(@RequestBody @Validated RoleAssignIdsDTO dto) throws BizException {
        manageRoleInfoService.assignRoleMenu(dto.getRoleCode(), dto.getIdList());
        return Result.ok();
    }

    @PostMapping("/assign-api")
    public Result<Void> assignRoleApi(@RequestBody @Validated RoleAssignIdsDTO dto) throws BizException {
        manageRoleInfoService.assignRoleApi(dto.getRoleCode(), dto.getIdList());
        return Result.ok();
    }
}
