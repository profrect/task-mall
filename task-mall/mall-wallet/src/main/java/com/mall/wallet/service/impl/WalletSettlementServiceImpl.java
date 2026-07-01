package com.mall.wallet.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.model.entity.WalletFlowDetail;
import com.mall.wallet.service.WalletAccountService;
import com.mall.wallet.service.WalletSettlementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/** 写入型钱包结算服务：所有奖励/扣款类跨服务资金动作统一进入账务内核。 */
@Service
public class WalletSettlementServiceImpl implements WalletSettlementService {

    private static final String DEFAULT_CURRENCY = "USDT";

    private static final Set<WalletBizType> SETTLEMENT_TYPES = EnumSet.of(
            WalletBizType.TASK_REWARD,
            WalletBizType.TASK_CENTER_REWARD,
            WalletBizType.VIP_UPGRADE,
            WalletBizType.INVITE_COMMISSION,
            WalletBizType.LOTTERY_REWARD,
            WalletBizType.CHECKIN_REWARD,
            WalletBizType.SHARE_TASK_REWARD);

    @Resource
    private WalletAccountService walletAccountService;

    @Override
    public WalletSettlementResp apply(WalletSettlementReq req) throws BizException {
        Preconditions.notNull(req, WalletRespCodeEnum.SETTLEMENT_REQUEST_INVALID);
        Preconditions.notNull(req.getUserId(), WalletRespCodeEnum.SETTLEMENT_REQUEST_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getBizType()), WalletRespCodeEnum.SETTLEMENT_BIZ_TYPE_INVALID, req.getBizType());
        Preconditions.needTrue(StringUtils.hasText(req.getBizId()), WalletRespCodeEnum.SETTLEMENT_REQUEST_INVALID);

        WalletBizType bizType = parseBizType(req.getBizType());
        Preconditions.needTrue(SETTLEMENT_TYPES.contains(bizType),
                WalletRespCodeEnum.SETTLEMENT_BIZ_TYPE_INVALID, req.getBizType());
        String currency = normalizeCurrency(req.getCurrency());
        walletAccountService.getOrCreate(req.getUserId(), currency);
        WalletFlowDetail flow = walletAccountService.applyLedger(new LedgerEvent(
                req.getUserId(), currency, bizType, req.getBizId().trim(), req.getAmount(), req.getRemark()));
        WalletSettlementResp resp = toResp(flow);
        resp.setCurrency(currency);
        return resp;
    }

    private WalletBizType parseBizType(String value) throws BizException {
        try {
            return WalletBizType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BizException(WalletRespCodeEnum.SETTLEMENT_BIZ_TYPE_INVALID, new Object[]{value});
        }
    }

    private String normalizeCurrency(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY;
    }

    private WalletSettlementResp toResp(WalletFlowDetail f) {
        WalletSettlementResp resp = new WalletSettlementResp();
        resp.setFlowNo(f.getFlowNo());
        resp.setUserId(f.getUserId());
        resp.setBizType(f.getBizType());
        resp.setBizId(f.getBizId());
        resp.setDirection(f.getDirection());
        resp.setAmount(f.getChangeAmt());
        resp.setBalanceBefore(f.getBalanceBefore());
        resp.setBalanceAfter(f.getBalanceAfter());
        resp.setRemark(f.getRemark());
        resp.setCreateTime(f.getCreateTime());
        return resp;
    }
}