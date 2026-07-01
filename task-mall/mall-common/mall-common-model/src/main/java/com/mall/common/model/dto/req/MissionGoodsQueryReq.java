package com.mall.common.model.dto.req;

import com.mall.common.model.dto.BasePageDTO;
import lombok.Data;

/** 后台/Provider 查询商品或投资展示配置。 */
@Data
public class MissionGoodsQueryReq extends BasePageDTO {

    private String goodsType;

    private String keyword;

    private Integer status;
}