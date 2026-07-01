package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台/Provider 查询抽奖奖品配置。 */
@Data
public class PromotionPrizeQueryReq extends BasePageDTO {

    private String keyword;

    private String prizeType;

    private Integer status;
}