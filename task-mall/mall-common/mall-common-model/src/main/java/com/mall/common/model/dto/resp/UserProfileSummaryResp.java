package com.mall.common.model.dto.resp;

import lombok.Data;

/** 用户公开展示资料：用于跨服务榜单脱敏展示。 */
@Data
public class UserProfileSummaryResp {

    private Long userId;

    private String displayName;

    private Integer vipLevel;

    private Integer status;
}