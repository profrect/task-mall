package com.mall.common.core.result;

import com.mall.common.core.resp.CommonRespCode;
import com.mall.common.core.resp.RespCode;
import lombok.Data;

import java.util.Objects;

@Data
public class Result<T> {

    private final int code;

    private final String msg;

    private final T data;

    private Boolean success;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(RespCode respCode, T data) {
        this(respCode.getCode(), respCode.getMsg(), data);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(CommonRespCode.OK, data);
    }

    public static <T> Result<T> fail() {
        return fail(CommonRespCode.SYSTEM_ERROR);
    }

    public static <T> Result<T> fail(RespCode respCode) {
        return fail(respCode, null);
    }

    public static <T> Result<T> fail(RespCode respCode, T data) {
        return fail(respCode.getCode(), respCode.getMsg(), data);
    }

    public static <T> Result<T> fail(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public Boolean getSuccess() {
        if (Objects.isNull(success)) {
            return code == CommonRespCode.OK.getCode();
        }
        return success;
    }
}
