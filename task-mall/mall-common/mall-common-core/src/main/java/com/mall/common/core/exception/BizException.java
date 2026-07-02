package com.mall.common.core.exception;

import com.mall.common.core.resp.CommonRespCode;
import com.mall.common.core.resp.RespCode;
import lombok.Data;

@Data
public class BizException extends Exception {

    private RespCode respCode;

    private Object[] args;

    public BizException() {
        this(CommonRespCode.SYSTEM_ERROR, null);
    }

    public BizException(RespCode respCode, Object[] args) {
        super(respCode.getMsg());
        this.respCode = respCode;
        this.args = args;
    }

    public BizException(int code, String msg) {
        super(msg);
        this.respCode = new SimpleRespCode(code, msg);
    }

    private record SimpleRespCode(int code, String msg) implements RespCode {
        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMsg() {
            return msg;
        }
    }
}
