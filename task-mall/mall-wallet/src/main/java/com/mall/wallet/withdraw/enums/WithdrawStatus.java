package com.mall.wallet.withdraw.enums;

/**
 * 提现订单状态机。各状态对应一次确定的账务动作（由 WithdrawService 编排，状态与账务同事务推进）：
 * <pre>
 *   apply              approve              broadcast ok          confirms >= N
 * REVIEWING ───────────► APPROVED ───────────► BROADCASTING ───────────► CONFIRMED(终态 ✓)
 *   │ FREEZE                                         │                         SETTLE
 *   ├── reject ──► REJECTED(终态, UNFREEZE)          └── broadcast/chain fail ──► FAILED(终态, UNFREEZE)
 * </pre>
 * 终态：CONFIRMED（成功，已结算）、REJECTED（人工驳回，已解冻）、FAILED（出款失败，已解冻）。
 */
public enum WithdrawStatus {

    /** 已申请、可用余额已冻结，等待人工审核。 */
    REVIEWING,

    /** 人工审核通过，等待出款广播（瞬态）。 */
    APPROVED,

    /** 已签名广播上链，等待确认数达标。 */
    BROADCASTING,

    /** 链上确认达标、已结算（冻结扣减、总额减少）。终态。 */
    CONFIRMED,

    /** 人工驳回，冻结已解冻退回可用。终态。 */
    REJECTED,

    /** 出款失败（广播失败/链上失败），冻结已解冻退回可用。终态。 */
    FAILED
}