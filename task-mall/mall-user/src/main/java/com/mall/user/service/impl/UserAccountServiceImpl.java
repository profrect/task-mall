package com.mall.user.service.impl;

import com.mall.user.model.dto.UserChangePwdDTO;
import com.mall.user.model.dto.UserLoginDTO;
import com.mall.user.model.dto.UserRegisterDTO;
import com.mall.user.model.vo.UserLoginVO;
import com.mall.user.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {
    @Override
    public void userRegister(UserRegisterDTO registerDTO) {

    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        return null;
    }

    @Override
    public void passwordChange(UserChangePwdDTO changePwdDTO) {

    }
}
