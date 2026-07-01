package com.mall.wallet.enums;

/**
 * 资金方向：账务内核中余额变换的最小语义单元。
 * 账户不变式 total = avail + frozen 在每种方向下都必须保持。
 */
public enum LedgerDirection {

    /** 入账：可用 +amt，总额 +amt */
    IN,

    /** 出账：可用 -amt，总额 -amt（要求可用充足） */
    OUT,

    /** 冻结：可用 -amt，冻结 +amt（总额不变，要求可用充足） */
    FREEZE,

    /** 解冻：冻结 -amt，可用 +amt（总额不变，要求冻结充足） */
    UNFREEZE,

    /** 冻结结算出账：冻结 -amt，总额 -amt（可用不变，要求冻结充足；用于提现通过） */
    SETTLE
}