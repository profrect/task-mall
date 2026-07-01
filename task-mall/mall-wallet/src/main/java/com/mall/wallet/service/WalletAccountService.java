package com.mall.wallet.service;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.WalletStatsResp;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.model.entity.WalletAccount;
import com.mall.wallet.model.entity.WalletFlowDetail;

import java.util.List;

/**
 * 账务内核：系统中唯一允许改动钱包余额的入口。
 * 业务编排（充值/提现/奖励等）只构造 LedgerEvent 并调用 applyLedger，不直接触碰余额字段。
 */
public interface WalletAccountService {

    /**
     * 读取或初始化账户（幂等，处理并发首建）。
     * 账户开通与账务变动分离：本方法负责"账户存在性"，applyLedger 负责"余额变动"。
     */
    WalletAccount getOrCreate(Long userId, String currency) throws BizException;

    /**
     * 只读批量账户快照：供管理端会员列表聚合余额；不触发账户懒创建，未开户用户由调用方按 0 处理。
     */
    List<WalletAccount> listByUserIds(List<Long> userIds, String currency) throws BizException;

    /** 钱包域统计快照：余额、充值、提现、归集，只读不改状态。 */
    WalletStatsResp stats(String currency, long todayStartAt) throws BizException;

    /**
     * 账务原语：对账户施加一次账务事件。
     * 行锁 + 幂等(bizType+bizId) + 同事务写流水，是所有资金动作的统一实现。
     *
     * @return 本次产生的流水；若幂等命中（该业务已入账过），返回已存在的流水。
     */
    WalletFlowDetail applyLedger(LedgerEvent event) throws BizException;
}