package com.mall.common.model.dto.resp;

import lombok.Data;

/** 后台模拟登录票据响应。ticket 只返回一次，不落库明文。 */
@Data
public class UserImpersonationTicketResp {

    private String ticket;

    private Integer expiresIn;

    private Long expiresAt;
}