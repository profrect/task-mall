package com.mall.user.enums;

import com.mall.common.core.resp.RespCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserRespCodeEnum implements RespCode {

    ACCOUNT_EXISTS(21001, "账号已存在：%s"),
    USER_NOT_EXIST(21002, "用户不存在"),
    ACCOUNT_OR_PASSWORD_ERROR(21003, "账号或密码错误"),
    INVITE_CODE_INVALID(21004, "邀请码无效"),
    OLD_PASSWORD_ERROR(21005, "旧密码错误"),
    ACCOUNT_FROZEN(21006, "账号已被冻结"),
    USER_STATUS_INVALID(21007, "用户状态无效"),
    VIP_LEVEL_INVALID(21008, "VIP等级无效"),
    VIP_CONFIG_INVALID(21009, "VIP配置无效"),
    VIP_CONFIG_NOT_FOUND(21010, "VIP配置不存在：%s"),
    VIP_LEVEL_REPEAT(21011, "VIP等级已存在：%s"),
    VIP_BASE_LEVEL_LOCKED(21012, "基础VIP等级不能删除"),
    ;

    private final Integer code;

    private final String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}