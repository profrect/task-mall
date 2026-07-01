package com.mall.common.model.dto.req;

import lombok.Data;

/** 会员状态变更请求（跨服务契约：mall-admin → mall-user provider）。 */
@Data
public class UserStatusUpdateReq {

    private Long userId;

    /** 1=正常，2=冻结。 */
    private Integer status;
}