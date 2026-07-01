package com.mall.wallet.chain.adapter;

import com.mall.wallet.chain.ChainCode;
import lombok.Builder;

/**
 * 链上充值观测事件（适配层 → 编排层的边界 DTO）。
 * <p>
 * amountRaw 为链上原始整数单位字符串（不做精度假设，归一化由编排层按 coin_config.decimals 处理）。
 * confirmations 由各链适配器在观测时自行计算（如 当前链高 - 交易区块高 + 1），编排层据此判断是否达标入账。
 */
@Builder
public record DepositEvent(
        ChainCode chainCode,
        String contractAddress,
        String txHash,
        int logIndex,
        String fromAddress,
        String toAddress,
        String amountRaw,
        long blockHeight,
        long confirmations
) {
}