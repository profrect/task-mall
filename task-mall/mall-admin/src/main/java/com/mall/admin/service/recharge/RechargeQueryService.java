package com.mall.admin.service.recharge;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.RechargeOrderResp;

import java.util.List;

/**
 * 充值订单查询（管理端）：把管理端的充值监控查询翻译为对 mall-wallet recharge provider 的跨服务调用。
 * <p>
 * 充值是链上自动入账（扫块达确认数即记账），管理端为只读监控——
 * 无审批（区别于 withdraw）、无触发（区别于 collect），仅按状态/限量查询订单。
 */
public interface RechargeQueryService {

    /** 充值订单列表：status 留空查全部（CONFIRMING/CREDITED），userId 可选，最新在前，limit 限量。 */
    List<RechargeOrderResp> list(String status, Long userId, Integer limit) throws BizException;
}