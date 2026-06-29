//package com.mall.admin.controller;
//
//import com.mall.admin.model.dto.ManageApiInfoDTO;
//import com.mall.admin.model.vo.ManageApiInfoVO;
//import com.mall.admin.model.vo.ServerApiListVO;
//import com.mall.admin.service.sys.ManageApiInfoService;
//import com.mall.common.core.exception.BizException;
//import com.mall.common.core.result.Result;
//import com.mall.common.core.valid.ValidGroups;
//import com.mybatisflex.core.paginate.Page;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/sys/manage/server-api")
//public class ManageApiInfoController {
//
//    @Resource
//    private ManageApiInfoService manageApiInfoService;
//
//    @GetMapping("/page-list")
//    public Result<Page<ManageApiInfoVO>> pageList(ManageApiInfoDTO dto) {
//        return Result.ok(manageApiInfoService.pageList(dto));
//    }
//
//    @PostMapping("/add")
//    public Result<Void> add(@RequestBody @Validated({ValidGroups.Insert.class}) ManageApiInfoDTO dto) throws BizException {
//        manageApiInfoService.add(dto);
//        return Result.ok();
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public Result<Void> remove(@PathVariable("id") @NotNull(message = "用户id不能为空") Long id) throws BizException {
//        manageApiInfoService.delete(id);
//        return Result.ok();
//    }
//
//    @PostMapping("/update")
//    public Result<Void> update(@RequestBody @Validated({ValidGroups.Update.class}) ManageApiInfoDTO dto) throws BizException {
//        manageApiInfoService.update(dto);
//        return Result.ok();
//    }
//
//    @GetMapping("/getAllTypeApis")
//    public Result<List<ServerApiListVO>> getAllTypeApis() {
//        return Result.ok(manageApiInfoService.getAllTypeApis());
//    }
//
//    @PostMapping("/upload")
//    public Result<Void> upload(@RequestPart("file") MultipartFile file){
//        manageApiInfoService.importServerApi(file);
//        return Result.ok();
//    }
//
//    @GetMapping("/download")
//    public void download(HttpServletResponse response) throws BizException {
//        manageApiInfoService.exportServerApi(response);
//    }
//}
