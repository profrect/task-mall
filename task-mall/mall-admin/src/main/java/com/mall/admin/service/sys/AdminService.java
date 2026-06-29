package com.mall.admin.service.sys;


import com.mall.admin.model.dto.AdminLoginDTO;
import com.mall.admin.model.entity.ManageUserInfo;
import com.mall.admin.model.vo.AdminLoginVO;
import com.mall.common.core.exception.BizException;

public interface AdminService {

    /**
     *  管理员登录
     * @param dto dto
     * @return vo
     */
    AdminLoginVO login(AdminLoginDTO dto) throws BizException;

    /**
     * 管理员注销登录
     */
    void logout();

    /**
     * 用户名和密码校验
     * @param username 用户名
     * @param userPswd 密码
     * @return po
     * @throws BizException bizException
     */
    ManageUserInfo doCheckUserAndPassword(String username, String userPswd) throws BizException;
}
