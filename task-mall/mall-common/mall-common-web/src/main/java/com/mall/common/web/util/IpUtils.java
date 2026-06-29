package com.mall.common.web.util;

import com.mall.common.core.cons.CommonConst;
import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    private static final String[] HEADERS = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};

    public static String getRealIp(HttpServletRequest request) {
        for (String header : HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !CommonConst.UNKNOWN.equalsIgnoreCase(ip)) {
                return ip.split(CommonConst.CHAR_COMMA)[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private IpUtils() {
        throw new UnsupportedOperationException("工具类[IpUtils]不允许额外实例化");
    }
}
