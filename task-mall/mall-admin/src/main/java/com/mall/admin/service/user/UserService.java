package com.mall.admin.service.user;

import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.UserAdminSaveReq;
import com.mall.common.model.dto.req.UserGroupAssignReq;
import com.mall.common.model.dto.req.UserGroupReq;
import com.mall.common.model.dto.req.UserLineChangeReq;
import com.mall.common.model.dto.resp.UserGroupResp;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mall.common.model.dto.resp.UserLineageResp;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface UserService {

    Page<UserInfoVO> userPage(UserInfoDTO dto) throws BizException;

    UserInfoVO userDetail(Long userId) throws BizException;

    UserInfoVO save(UserAdminSaveReq req) throws BizException;

    List<UserInfoVO> export(UserInfoDTO dto) throws BizException;

    List<UserGroupResp> groupList(Integer status) throws BizException;

    UserGroupResp saveGroup(UserGroupReq req) throws BizException;

    void deleteGroup(Long id) throws BizException;

    void assignGroup(UserGroupAssignReq req) throws BizException;

    UserLineageResp lineage(Long userId) throws BizException;

    void changeParent(UserLineChangeReq req) throws BizException;

    void updateStatus(Long userId, Integer status) throws BizException;

    void updateStatusBatch(List<Long> userIds, Integer status) throws BizException;

    void logout(Long userId) throws BizException;

    UserImpersonationTicketResp createImpersonationTicket(Long userId, String adminIp, String userAgent) throws BizException;
}
