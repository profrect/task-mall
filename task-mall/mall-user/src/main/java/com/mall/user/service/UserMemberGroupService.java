package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.UserGroupAssignReq;
import com.mall.common.model.dto.req.UserGroupReq;
import com.mall.common.model.dto.resp.UserGroupResp;

import java.util.List;

public interface UserMemberGroupService {

    List<UserGroupResp> list(Integer status);

    UserGroupResp save(UserGroupReq req) throws BizException;

    void delete(Long id) throws BizException;

    void assign(UserGroupAssignReq req) throws BizException;
}