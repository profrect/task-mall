package com.mall.admin.controller.user;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.UserAdminSaveReq;
import com.mall.common.model.dto.req.UserBatchStatusUpdateReq;
import com.mall.common.model.dto.req.UserGroupAssignReq;
import com.mall.common.model.dto.req.UserGroupReq;
import com.mall.common.model.dto.req.UserLineChangeReq;
import com.mall.common.model.dto.req.UserStatusUpdateReq;
import com.mall.common.model.dto.resp.UserGroupResp;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mall.common.model.dto.resp.UserLineageResp;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/detail/{userId}")
    public Result<UserInfoVO> userDetail(@PathVariable("userId") Long userId) throws BizException {
        return Result.ok(userService.userDetail(userId));
    }

    @PostMapping("/save")
    public Result<UserInfoVO> save(@RequestBody UserAdminSaveReq req) throws BizException {
        return Result.ok(userService.save(req));
    }

    @GetMapping("/export")
    public Result<List<UserInfoVO>> export(UserInfoDTO dto) throws BizException {
        return Result.ok(userService.export(dto));
    }

    @GetMapping("/group/list")
    public Result<List<UserGroupResp>> groupList(@RequestParam(name = "status", required = false) Integer status) throws BizException {
        return Result.ok(userService.groupList(status));
    }

    @PostMapping("/group/save")
    public Result<UserGroupResp> saveGroup(@RequestBody UserGroupReq req) throws BizException {
        return Result.ok(userService.saveGroup(req));
    }

    @PostMapping("/group/delete")
    public Result<Void> deleteGroup(@RequestBody IdReq req) throws BizException {
        userService.deleteGroup(req.getId());
        return Result.ok();
    }

    @PostMapping("/group/assign")
    public Result<Void> assignGroup(@RequestBody UserGroupAssignReq req) throws BizException {
        userService.assignGroup(req);
        return Result.ok();
    }

    @GetMapping("/line/{userId}")
    public Result<UserLineageResp> lineage(@PathVariable("userId") Long userId) throws BizException {
        return Result.ok(userService.lineage(userId));
    }

    @PostMapping("/line/change")
    public Result<Void> changeParent(@RequestBody UserLineChangeReq req) throws BizException {
        userService.changeParent(req);
        return Result.ok();
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody UserStatusUpdateReq req) throws BizException {
        userService.updateStatus(req.getUserId(), req.getStatus());
        return Result.ok();
    }

    @PostMapping("/batch-status")
    public Result<Void> updateStatusBatch(@RequestBody UserBatchStatusUpdateReq req) throws BizException {
        userService.updateStatusBatch(req.getUserIds(), req.getStatus());
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody IdReq req) throws BizException {
        userService.logout(req.getId());
        return Result.ok();
    }

    @PostMapping("/impersonation-ticket")
    public Result<UserImpersonationTicketResp> createImpersonationTicket(
            @RequestBody IdReq req, HttpServletRequest request) throws BizException {
        return Result.ok(userService.createImpersonationTicket(
                req.getId(), clientIp(request), request.getHeader("User-Agent")));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}