package com.mall.admin.enums;

import com.mall.common.core.resp.RespCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AdminRespCodeEnum implements RespCode {

    DATA_NOT_FOUND(20000, "数据不存在：%s"),
    USER_NOT_EXIST(20001, "用户不存在"),
    PASSWORD_ERROR(20002, "密码错误"),
    USER_NAME_REPEAT(20003, "用户名重复：%s"),

    ROLE_CODE_REPEAT(20004, "角色代码重复：%s"),

    ROLE_NOT_EXIST(20005, "角色信息不存在"),

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
