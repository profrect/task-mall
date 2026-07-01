package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台查询邀请返佣记录。 */
@Data
public class InviteCommissionRecordQueryReq extends BasePageDTO {

    private Long inviterUserId;

    private Long sourceUserId;

    private String sourceOrderNo;

    private String businessType;

    private String status;

    private String keyword;
}