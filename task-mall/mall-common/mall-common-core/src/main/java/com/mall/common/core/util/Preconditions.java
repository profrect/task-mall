package com.mall.common.core.util;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.RespCode;

public class Preconditions {

    public static void notNull(Object obj, RespCode respCode, Object... args) throws BizException {
        if(obj == null) {
            throw new BizException(respCode, args);
        }
    }

    public static void needTrue(boolean cond, RespCode respCode, Object... args) throws BizException {
        if(!cond) {
            throw new BizException(respCode, args);
        }
    }
}
