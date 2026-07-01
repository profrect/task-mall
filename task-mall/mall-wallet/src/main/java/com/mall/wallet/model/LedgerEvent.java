package com.mall.wallet.model;

import com.mall.wallet.enums.WalletBizType;

import java.math.BigDecimal;

/**
 * 账务事件：账务内核 applyLedger 的唯一输入。
 * <p>
 * 一切资金动作（充值/提现/奖励/转账/扣款）都表达为一个 LedgerEvent；
 * 资金方向由 bizType 决定，调用方不接触余额字段如何变换，从而避免参数爆发与方向传错。
 *
 * @param userId   目标钱包用户
 * @param currency 币种（为空时默认 USDT）
 * @param bizType  业务类型（自带资金方向与默认摘要）
 * @param bizId    业务幂等键（充值=txHash[:logIndex]；提现/转账=订单号）
 * @param amount   变动金额（恒为正）
 * @param remark   摘要（为空时取 bizType 默认摘要）
 */
public record LedgerEvent(
        Long userId,
        String currency,
        WalletBizType bizType,
        String bizId,
        BigDecimal amount,
        String remark
) {

    public static LedgerEvent of(Long userId, WalletBizType bizType, String bizId, BigDecimal amount) {
        return new LedgerEvent(userId, null, bizType, bizId, amount, null);
    }

    public static LedgerEvent of(Long userId, WalletBizType bizType, String bizId, BigDecimal amount, String remark) {
        return new LedgerEvent(userId, null, bizType, bizId, amount, remark);
    }
}