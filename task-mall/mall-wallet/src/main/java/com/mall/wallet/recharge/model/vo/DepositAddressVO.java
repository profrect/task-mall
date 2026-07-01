package com.mall.wallet.recharge.model.vo;

import lombok.Builder;

/**
 * 充值地址视图：仅暴露链与收款地址，绝不外泄私钥等敏感字段。
 */
@Builder
public record DepositAddressVO(
        String chainCode,
        String address
) {
}