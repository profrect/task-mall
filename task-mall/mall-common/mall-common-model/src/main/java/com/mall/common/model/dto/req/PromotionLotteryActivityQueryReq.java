package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台/Provider 查询抽奖活动。 */
@Data
public class PromotionLotteryActivityQueryReq extends BasePageDTO {

    private String keyword;

    private Integer status;
}