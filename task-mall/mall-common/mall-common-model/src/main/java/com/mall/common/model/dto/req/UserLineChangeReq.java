package com.mall.common.model.dto.req;

import lombok.Data;

@Data
public class UserLineChangeReq {

    private Long userId;

    /** 新上级会员 userId。 */
    private Long parentUserId;
}