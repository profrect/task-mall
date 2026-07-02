package com.mall.common.model.dto.req;

import lombok.Data;

/** 后台创建会员模拟登录票据请求（mall-admin → mall-user provider）。 */
@Data
public class UserImpersonationTicketReq {

    private Long userId;

    private String adminAccount;

    private String adminIp;

    private String userAgent;
}