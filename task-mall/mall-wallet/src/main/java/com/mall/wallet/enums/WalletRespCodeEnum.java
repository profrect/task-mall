package com.mall.wallet.enums;

import com.mall.common.core.resp.RespCode;
import lombok.AllArgsConstructor;

/**
 * 钱包模块业务返回码（30000 段）。
 */
@AllArgsConstructor
public enum WalletRespCodeEnum implements RespCode {

    ACCOUNT_NOT_FOUND(30000, "钱包账户不存在：%s"),
    INVALID_AMOUNT(30001, "金额非法：必须大于0且小数位不超过%s位"),
    INSUFFICIENT_BALANCE(30002, "可用余额不足"),
    FROZEN_BALANCE_INVALID(30003, "冻结余额不足"),

    CHAIN_NOT_SUPPORTED(30010, "暂不支持的链：%s"),
    CHAIN_SCAN_ERROR(30011, "链上扫块失败：%s"),
    ADDRESS_DERIVE_ERROR(30012, "充值地址派生失败：%s"),
    WITHDRAW_NOT_SUPPORTED(30013, "该链当前不支持提现"),
    COIN_CONFIG_MISSING(30014, "币种配置缺失：%s"),
    DEPOSIT_ADDRESS_UNAVAILABLE(30015, "充值地址暂不可用：%s"),

    WITHDRAW_DISABLED(30016, "提现功能未开放"),
    WITHDRAW_AMOUNT_TOO_SMALL(30017, "提现金额低于最小值：%s"),
    WITHDRAW_FEE_INVALID(30018, "提现金额不足以覆盖手续费"),
    WITHDRAW_ADDRESS_INVALID(30019, "提现地址非法：%s"),
    HOT_WALLET_NOT_CONFIGURED(30020, "热钱包未配置：%s"),
    WITHDRAW_ORDER_NOT_FOUND(30021, "提现订单不存在：%s"),
    WITHDRAW_STATUS_ILLEGAL(30022, "提现订单状态不允许此操作：当前 %s"),
    WITHDRAW_BROADCAST_ERROR(30023, "提现广播失败：%s"),

    COLLECT_BALANCE_EMPTY(30024, "归集余额为空或低于阈值：%s"),
    COLLECT_ORDER_NOT_FOUND(30025, "归集订单不存在：%s"),
    COLLECT_STATUS_ILLEGAL(30026, "归集订单状态不允许此操作：当前 %s"),
    GAS_STATION_NOT_CONFIGURED(30027, "gas 供给钱包未配置：%s"),
    COLLECT_IN_PROGRESS(30028, "该地址已有进行中的归集任务：%s"),

    SETTLEMENT_REQUEST_INVALID(30029, "钱包结算请求非法"),
    SETTLEMENT_BIZ_TYPE_INVALID(30030, "钱包结算业务类型非法：%s"),
    TRANSFER_USER_INVALID(30031, "转账用户非法"),
    TRANSFER_SELF_NOT_ALLOWED(30032, "不能向自己转账"),
    TRANSFER_RECEIVER_NOT_FOUND(30033, "收款用户不存在：%s"),
    TRANSFER_RECEIVER_NOT_AVAILABLE(30034, "收款用户不可用：%s"),
    MANUAL_RECHARGE_REFERENCE_DUPLICATE(30035, "人工充值补单凭证已存在：%s"),
    MANUAL_RECHARGE_INVALID(30036, "人工充值补单请求非法：%s"),
    ;

    private final Integer code;

    private final String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}