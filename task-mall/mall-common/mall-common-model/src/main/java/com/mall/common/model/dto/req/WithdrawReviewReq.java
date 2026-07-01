package com.mall.common.model.dto.req;

import lombok.Data;

/**
 * 提现审核指令（跨服务契约：mall-admin → mall-wallet provider）。
 * <p>
 * reviewer 由 admin 服务端从登录态解析后回填，绝不取自管理端前端输入，从根上杜绝审核人冒名。
 */
@Data
public class WithdrawReviewReq {

    /** 目标提现订单号。 */
    private String orderNo;

    /** 审核人（admin 用户名，由调用方服务端注入）。 */
    private String reviewer;

    /** 审核备注 / 驳回原因。 */
    private String remark;
}