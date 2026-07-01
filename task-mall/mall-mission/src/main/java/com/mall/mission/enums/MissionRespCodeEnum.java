package com.mall.mission.enums;

import com.mall.common.core.resp.RespCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MissionRespCodeEnum implements RespCode {

    GOODS_INVALID(23001, "商品/项目配置无效"),
    GOODS_NOT_FOUND(23002, "商品/项目不存在：%s"),
    TASK_INVALID(23003, "任务配置无效"),
    TASK_NOT_FOUND(23004, "任务不存在：%s"),
    TASK_NOT_AVAILABLE(23005, "任务不可领取"),
    USER_TASK_NOT_FOUND(23006, "用户任务记录不存在：%s"),
    USER_TASK_STATUS_INVALID(23007, "用户任务状态无效"),
    USER_TASK_DUPLICATE(23008, "任务已领取或已提交"),
    REVIEW_REQUEST_INVALID(23009, "审核请求无效"),
    SETTLEMENT_INVALID(23010, "任务奖励结算状态无效"),
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