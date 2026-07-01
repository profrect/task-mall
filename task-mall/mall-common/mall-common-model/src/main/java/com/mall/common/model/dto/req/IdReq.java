package com.mall.common.model.dto.req;

import lombok.Data;

/** 通用主键请求，供内部 provider 的非 GET 写操作复用。 */
@Data
public class IdReq {

    private Long id;
}