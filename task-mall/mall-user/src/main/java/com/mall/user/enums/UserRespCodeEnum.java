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
    IMPERSONATION_TICKET_INVALID(21013, "模拟登录票据无效"),
    IMPERSONATION_TICKET_EXPIRED(21014, "模拟登录票据已过期"),
    IMPERSONATION_ADMIN_INVALID(21015, "模拟登录管理员无效"),
    USER_BATCH_EMPTY(21016, "请选择会员"),
    USER_GROUP_NOT_FOUND(21017, "会员分组不存在"),
    USER_GROUP_NAME_EMPTY(21018, "会员分组名称不能为空"),
    USER_GROUP_NAME_EXISTS(21019, "会员分组已存在：%s"),
    USER_GROUP_IN_USE(21020, "会员分组仍有成员，不能删除"),
    ACCOUNT_REQUIRED(21021, "会员账号不能为空"),
    PASSWORD_REQUIRED(21022, "会员密码不能为空"),
    USER_PARENT_INVALID(21023, "上级会员无效"),
    USER_LINE_CYCLE(21024, "不能把会员调整到自己的下级链路中"),
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