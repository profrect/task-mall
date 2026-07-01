package com.mall.common.model.dto.resp;

import lombok.Data;

/** 用户存在性查询响应。 */
@Data
public class UserExistResp {

    private Long userId;

    private Boolean exists;

    private Integer status;
}