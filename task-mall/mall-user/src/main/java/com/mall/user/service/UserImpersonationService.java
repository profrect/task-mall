package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.UserImpersonationExchangeReq;
import com.mall.common.model.dto.req.UserImpersonationTicketReq;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mall.user.model.vo.UserLoginVO;

/** 后台模拟会员登录前台：签发一次性票据，并由前台兑换为带审计标记的用户会话。 */
public interface UserImpersonationService {

    UserImpersonationTicketResp createTicket(UserImpersonationTicketReq req) throws BizException;

    UserLoginVO exchange(UserImpersonationExchangeReq req) throws BizException;
}