package com.mall.admin.service.user;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mybatisflex.core.paginate.Page;

public interface UserService {

    Page<UserInfoVO> userPage(UserInfoDTO dto) throws BizException;

    void updateStatus(Long userId, Integer status) throws BizException;

    UserImpersonationTicketResp createImpersonationTicket(Long userId, String adminIp, String userAgent) throws BizException;
}
