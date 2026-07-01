package com.mall.common.model.dto.req;

import lombok.Data;

import java.util.List;

/** 用户公开资料批量查询请求：只用于跨服务只读展示，不包含敏感字段。 */
@Data
public class UserProfileSummaryBatchReq {

    private List<Long> userIds;
}