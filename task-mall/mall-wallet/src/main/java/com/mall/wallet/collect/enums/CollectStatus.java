package com.mall.wallet.collect.enums;

/**
 * 归集订单状态机。归集 = 把用户充值地址上的 USDT 搬到出款热钱包，是平台内部资金搬运，
 * <strong>不触发用户账务</strong>（用户余额在充值入账时已确定）。
 * <pre>
 *            create         native 余额够        sweep confirms >= N
 * CREATED ───────────────┬──直接归集广播──► SWEEPING ─────────────────► COMPLETED(终态 ✓)
 *                        │
 *                        └─native 不足──► GAS_FUNDING ──gas 确认达标──► (归集广播) ──► SWEEPING
 *   任一链上步骤广播失败 / 重试累计超限 ─────────────────────────────────► FAILED(终态)
 * </pre>
 * 幂等三重保障（故无需提现那样的瞬态隔离）：
 * <ul>
 *   <li>归集转「广播时刻实时全部余额」，余额为 0 直接跳过 —— 重复归集第二次必为空，不会重复转钱；</li>
 *   <li>喂 gas 前查 native 余额，已达阈值则跳过 —— 重复喂 gas 至多浪费极少 gas（gas 钱包出，非用户资金）；</li>
 *   <li>状态 CAS：每条边 {@code WHERE status=旧态}，落空即已被推进。</li>
 * </ul>
 * 与提现的本质差异：归集是「同向搬运」（用户地址→热钱包），重复广播不造成资金损失，故可省去瞬态。
 */
public enum CollectStatus {

    /** 订单已创建，待处理：下一步据 native 余额决定「喂 gas」还是「直接归集」。 */
    CREATED,

    /** 已广播 gas 代付交易，等待 gas 到账确认后再发起归集。 */
    GAS_FUNDING,

    /** 已广播归集交易，等待确认数达标。 */
    SWEEPING,

    /** 归集确认达标。终态。 */
    COMPLETED,

    /** 归集失败（广播失败 / 链上失败 / 重试超限）。终态，可人工新建订单重试。 */
    FAILED
}