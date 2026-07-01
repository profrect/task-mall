package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台查询用户抽奖记录。 */
@Data
public class PromotionLotteryRecordQueryReq extends BasePageDTO {

    private Long userId;

    private Long activityId;

    private String status;

    private String keyword;
}