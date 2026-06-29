package com.mall.admin.controller;

import com.mall.admin.model.dto.ManageMenuInfoDTO;
import com.mall.admin.model.vo.MenuSampleVO;
import com.mall.admin.model.vo.MenuTagVO;
import com.mall.admin.model.vo.MenuTreeVO;
import com.mall.admin.service.sys.ManageMenuInfoService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.core.valid.ValidGroups;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 系统菜单资源信息表 控制层
 *
 * @author xiaoc
 */
@RestController
@RequestMapping("/api/sys/manage/menu")
public class ManageMenuInfoController {

    @Resource
    private ManageMenuInfoService manageMenuInfoService;

    @GetMapping("/list")
    public Result<List<MenuSampleVO>> list() {
        return Result.ok(manageMenuInfoService.queryList());
    }

    @GetMapping("/tree-list")
    public Result<List<MenuTreeVO>> treeList(ManageMenuInfoDTO dto) {
        return Result.ok(manageMenuInfoService.queryTreeList(dto));
    }

    @GetMapping("/all-tree-list")
    public Result<MenuTagVO> allMenuTree() {
        return Result.ok(manageMenuInfoService.queryAllMenuTree());
    }

    @GetMapping("/ids/{roleCode}")
    public Result<MenuTagVO> ids(@PathVariable("roleCode") String roleCode) {
        return Result.ok(manageMenuInfoService.queryAllMenuIds(roleCode));
    }

    @PostMapping("/add")
    public Result<Void> addMenu(@RequestBody @Validated({ValidGroups.Insert.class}) ManageMenuInfoDTO dto) throws BizException {
        manageMenuInfoService.add(dto);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result<Void> updateMenu(@RequestBody @Validated({ValidGroups.Update.class}) ManageMenuInfoDTO dto) throws BizException {
        manageMenuInfoService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMenu(@PathVariable("id") @NotNull(message = "菜单id不能为空") Long id) throws BizException {
        manageMenuInfoService.delete(id);
        return Result.ok();
    }
}
