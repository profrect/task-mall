package com.mall.wallet.model.vo;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * 钱包账户概览。余额口径：total = avail + frozen。
 */
@Builder
public record WalletAccountVO(
        Long userId,
        String currency,
        BigDecimal totalBalance,
        BigDecimal availBalance,
        BigDecimal frozenBalance
) {
}