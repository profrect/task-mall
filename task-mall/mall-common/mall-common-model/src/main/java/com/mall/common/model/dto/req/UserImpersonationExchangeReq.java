package com.mall.common.model.dto.req;

import lombok.Data;

/** 前台用一次性票据兑换模拟登录 token。 */
@Data
public class UserImpersonationExchangeReq {

    private String ticket;
}