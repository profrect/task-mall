package com.mall.wallet.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.WalletSettlementResp;

/** 写入型钱包结算 provider 的本地服务。 */
public interface WalletSettlementService {

    /**
     * 执行业务结算。业务侧只传 bizType/bizId/amount，资金方向由钱包枚举固定。
     * 重复调用同一 bizType + bizId 会返回已存在流水。
     */
    WalletSettlementResp apply(WalletSettlementReq req) throws BizException;
}