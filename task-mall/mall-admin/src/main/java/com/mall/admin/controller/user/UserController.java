package com.mall.admin.controller.user;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.req.IdReq;
import com.mall.common.model.dto.req.UserStatusUpdateReq;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody UserStatusUpdateReq req) throws BizException {
        userService.updateStatus(req.getUserId(), req.getStatus());
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