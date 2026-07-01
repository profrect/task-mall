package com.mall.common.model.dto.req;

import lombok.Data;

/** 任务审核请求。 */
@Data
public class MissionTaskReviewReq {

    private Long recordId;

    private String reviewRemark;
}