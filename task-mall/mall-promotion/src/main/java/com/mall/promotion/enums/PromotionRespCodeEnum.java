package com.mall.promotion.enums;

import com.mall.common.core.resp.RespCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PromotionRespCodeEnum implements RespCode {

    PRIZE_INVALID(24001, "奖品配置无效"),
    PRIZE_NOT_FOUND(24002, "奖品不存在：%s"),
    PRIZE_STOCK_EMPTY(24003, "奖品库存不足"),
    ACTIVITY_INVALID(24004, "抽奖活动配置无效"),
    ACTIVITY_NOT_FOUND(24005, "抽奖活动不存在：%s"),
    ACTIVITY_NOT_AVAILABLE(24006, "抽奖活动暂不可参与"),
    LOTTERY_POOL_EMPTY(24007, "抽奖活动奖池为空"),
    LOTTERY_LIMIT_REACHED(24008, "今日抽奖次数已用完"),
    LOTTERY_RECORD_NOT_FOUND(24009, "抽奖记录不存在：%s"),
    LOTTERY_PRIZE_INVALID(24010, "抽奖奖池配置无效"),
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