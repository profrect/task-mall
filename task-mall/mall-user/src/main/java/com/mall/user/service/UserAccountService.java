package com.mall.user.service;

import com.mall.common.core.exception.BizException;
import com.mall.user.model.dto.UserChangePwdDTO;
import com.mall.user.model.dto.UserLoginDTO;
import com.mall.user.model.dto.UserRegisterDTO;
import com.mall.user.model.vo.UserLoginVO;

public interface UserAccountService {

    /**
     * 账号注册
     * @param registerDTO dto
     */
    void userRegister(UserRegisterDTO registerDTO) throws BizException;

    /**
     * 登录
     * @param loginDTO dto
     * @return vo
     */
    UserLoginVO login(UserLoginDTO loginDTO) throws BizException;

    /**
     * 密码修改
     * @param changePwdDTO dto
     */
    void passwordChange(UserChangePwdDTO changePwdDTO) throws BizException;
}
