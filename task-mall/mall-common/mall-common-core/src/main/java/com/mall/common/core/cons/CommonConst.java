package com.mall.common.core.cons;

public class CommonConst {

    public static final String LOG_TRACE_ID = "traceId";

    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final String CHAR_COMMA = ",";

    public static final String UNKNOWN = "unknown";

    private CommonConst(){
        throw new UnsupportedOperationException("常量类[CommonConst]不允许额外实例化");
    }
}
