package com.mall.common.model.dto.req;

import lombok.Data;

import java.util.List;

/** 钱包账户批量查询请求（provider 内部契约）。 */
@Data
public class WalletAccountBatchReq {

    private List<Long> userIds;

    private String currency;
}