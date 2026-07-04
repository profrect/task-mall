package com.mall.wallet.enums;

import lombok.Getter;

/**
 * 钱包业务类型。每种业务自带固定的资金方向与默认摘要：
 * 调用方只需声明“发生了什么业务”，无需也无法指定资金方向，从而消除方向传错的可能。
 * <p>
 * 幂等键为 (bizType, bizId)，因此同一笔业务的不同阶段须用不同 bizType，
 * 例如提现的 冻结 / 出账 / 解冻 三个阶段共享订单号但 bizType 不同，互不幂等覆盖。
 */
@Getter
public enum WalletBizType {

    RECHARGE(LedgerDirection.IN, "充值入账"),

    WITHDRAW_FREEZE(LedgerDirection.FREEZE, "提现申请冻结"),
    WITHDRAW_SETTLE(LedgerDirection.SETTLE, "提现出账"),
    WITHDRAW_UNFREEZE(LedgerDirection.UNFREEZE, "提现驳回解冻"),

    TRANSFER_IN(LedgerDirection.IN, "站内转账转入"),
    TRANSFER_OUT(LedgerDirection.OUT, "站内转账转出"),

    TASK_REWARD(LedgerDirection.IN, "任务收益入账"),
    TASK_CENTER_REWARD(LedgerDirection.IN, "任务中心奖励入账"),
    SHARE_TASK_REWARD(LedgerDirection.IN, "分享任务奖励入账"),
    VIDEO_TASK_REWARD(LedgerDirection.IN, "视频任务奖励入账"),
    VA_TASK_REWARD(LedgerDirection.IN, "VA任务奖励入账"),
    INVITE_COMMISSION(LedgerDirection.IN, "邀请返佣入账"),
    CHECKIN_REWARD(LedgerDirection.IN, "签到奖励入账"),
    LOTTERY_REWARD(LedgerDirection.IN, "抽奖奖励入账"),

    VIP_UPGRADE(LedgerDirection.OUT, "VIP升级扣款"),
    ;

    private final LedgerDirection direction;

    private final String defaultRemark;

    WalletBizType(LedgerDirection direction, String defaultRemark) {
        this.direction = direction;
        this.defaultRemark = defaultRemark;
    }
}