package com.mall.admin.service.collect;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.CollectOrderResp;

import java.util.List;

/**
 * 归集编排（管理端）：把管理端的归集触发/监控动作翻译为对 mall-wallet collect provider 的跨服务调用。
 * <p>
 * 归集本身是平台内部资金搬运（用户充值地址 → 出款热钱包），<strong>不涉及用户账务</strong>，
 * 状态机与链上广播全在 mall-wallet 内核，管理端只负责「触发」与「监控」。
 */
public interface CollectAdminService {

    /** 进行中归集订单（CREATED/GAS_FUNDING/SWEEPING），供监控面板。 */
    List<CollectOrderResp> activeList() throws BizException;

    /** 按状态查询归集订单（CREATED/GAS_FUNDING/SWEEPING/COMPLETED/FAILED）。 */
    List<CollectOrderResp> listByStatus(String status) throws BizException;

    /** 手工触发扫描建单：chainCode 留空则扫所有已接入链，返回新建订单数。 */
    int scan(String chainCode) throws BizException;

    /** 手工推进所有在途归集订单一轮（喂 gas → 归集广播 → 确认）。 */
    void advance() throws BizException;
}