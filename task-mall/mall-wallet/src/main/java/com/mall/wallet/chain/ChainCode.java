package com.mall.wallet.chain;

/**
 * 支持的链编码。与 chain_config.chain_code 一一对应。
 * 阶段1仅 TRON 落地；ETH/BSC/POLYGON 在阶段2接入 EVM 适配器。
 */
public enum ChainCode {
    TRON,
    ETH,
    BSC,
    POLYGON
}