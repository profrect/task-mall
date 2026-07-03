package com.mall.common.model.dto.req;

import lombok.Data;

import java.util.List;

/** 管理端批量会员状态变更请求（mall-admin → mall-user provider）。 */
@Data
public class UserBatchStatusUpdateReq {

    private List<Long> userIds;

    /** 1=正常，2=冻结。 */
    private Integer status;
}