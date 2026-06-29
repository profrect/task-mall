package com.mall.common.model.dto;

import lombok.Data;

@Data
public class BasePageDTO {

    private long pageNumber = 1;

    private long pageSize = 10;
}
