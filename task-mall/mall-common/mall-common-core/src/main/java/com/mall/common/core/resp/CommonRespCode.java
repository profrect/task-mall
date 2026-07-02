package com.mall.common.core.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonRespCode implements RespCode {

    OK(0, "OK"),

    // 参数检查错误
    PARAM_INVALID(10001, "common.error.param_invalid"),
    PARAM_MISSING(10002, "common.error.param_missing"),

    // 限流、幂等错误
    DUPLICATE_REQUEST(10003, "common.error.duplicate_request"),
    FREQUENT_REQUEST(10004, "common.error.frequent_request"),

    // 数据操作错误
    DATA_ACCESS_ERROR(10005, "common.error.data_access"),

    // 系统错误
    SYSTEM_ERROR(10008, "common.error.system_error"),
    SYSTEM_BUSY(10009, "common.error.system_busy"),

    // 认证和权限错误
    AUTH_DENIED(10010, "common.error.auth_denied"),
    PERMISSION_DENIED(10011, "common.error.permission_denied"),
    IMPERSONATION_READONLY(10012, "模拟登录仅允许查看"),
    ;

    private final int code;

    private final String msg;
}
