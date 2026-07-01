package com.mall.admin.model.dto;

import lombok.Data;

/**
 * 提现审核入参（管理端前端 → mall-admin）。
 * <p>
 * 不含 reviewer——审核人一律由服务端从登录态解析，前端无权指定。
 */
@Data
public class WithdrawReviewDTO {

    /** 目标提现订单号。 */
    private String orderNo;

    /** 审核备注 / 驳回原因。 */
    private String remark;
}