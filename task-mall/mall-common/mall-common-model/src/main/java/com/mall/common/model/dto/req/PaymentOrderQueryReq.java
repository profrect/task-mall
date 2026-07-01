package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 支付通道审计订单查询。 */
@Data
public class PaymentOrderQueryReq extends BasePageDTO {

    private Long userId;

    private String orderNo;

    private String businessType;

    private String channelCode;

    private String status;

    private Long startTime;

    private Long endTime;
}