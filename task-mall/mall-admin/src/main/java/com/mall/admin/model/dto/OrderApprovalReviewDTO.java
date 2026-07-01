package com.mall.admin.model.dto;

import lombok.Data;

/** 统一订单审核动作入参。 */
@Data
public class OrderApprovalReviewDTO {

    /** WITHDRAW / MISSION_TASK。 */
    private String sourceType;

    /** 提现使用 orderNo，任务使用 recordId 字符串。 */
    private String sourceId;

    private String remark;
}