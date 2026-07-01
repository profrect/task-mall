package com.mall.user.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.BasePageDTO;
import com.mall.common.model.dto.req.InviteCommissionRecordQueryReq;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.InviteCommissionRecordResp;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.user.configuration.properties.ServiceEndpointProperties;
import com.mall.user.enums.InviteCommissionStatus;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.InviteCommissionRecordMapper;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.mapper.VipLevelConfigMapper;
import com.mall.user.mapper.VipUpgradeOrderMapper;
import com.mall.user.model.entity.InviteCommissionRecord;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.entity.VipLevelConfig;
import com.mall.user.model.entity.VipUpgradeOrder;
import com.mall.user.service.InviteCommissionService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

/** 邀请返佣：用户服务持有邀请关系与返佣事实，钱包服务只负责幂等入账。 */
@Slf4j
@Service
public class InviteCommissionServiceImpl implements InviteCommissionService {

    private static final String BUSINESS_VIP_UPGRADE = "VIP_UPGRADE";
    private static final String WALLET_BIZ_TYPE = "INVITE_COMMISSION";
    private static final String ORDER_SUCCESS = "SUCCESS";
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final int CURRENCY_SCALE = 6;
    private static final String WALLET_SETTLEMENT_PATH = "/api/provider/wallet/settlement/apply";

    @Resource
    private InviteCommissionRecordMapper recordMapper;

    @Resource
    private VipUpgradeOrderMapper vipUpgradeOrderMapper;

    @Resource
    private VipLevelConfigMapper vipLevelConfigMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<InviteCommissionRecordResp> page(InviteCommissionRecordQueryReq req) throws BizException {
        InviteCommissionRecordQueryReq q = req == null ? new InviteCommissionRecordQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(InviteCommissionRecord.class)
                .eq(InviteCommissionRecord::getInviterUserId, q.getInviterUserId(), q.getInviterUserId() != null)
                .eq(InviteCommissionRecord::getSourceUserId, q.getSourceUserId(), q.getSourceUserId() != null)
                .eq(InviteCommissionRecord::getSourceOrderNo, trim(q.getSourceOrderNo()), StringUtils.hasText(q.getSourceOrderNo()))
                .eq(InviteCommissionRecord::getBusinessType, normalizeUpper(q.getBusinessType()), StringUtils.hasText(q.getBusinessType()))
                .eq(InviteCommissionRecord::getStatus, normalizeUpper(q.getStatus()), StringUtils.hasText(q.getStatus()))
                .and(w -> w.like(InviteCommissionRecord::getRecordNo, q.getKeyword())
                        .or(InviteCommissionRecord::getSourceOrderNo).like(q.getKeyword())
                        .or(InviteCommissionRecord::getWalletFlowNo).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(InviteCommissionRecord::getId, false);
        return recordMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper).map(this::toResp);
    }

    @Override
    public Page<InviteCommissionRecordResp> userPage(Long inviterUserId, BasePageDTO page) throws BizException {
        Preconditions.notNull(inviterUserId, UserRespCodeEnum.USER_NOT_EXIST);
        long pageNumber = page != null && page.getPageNumber() > 0 ? page.getPageNumber() : 1;
        long pageSize = page != null && page.getPageSize() > 0 ? page.getPageSize() : 10;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(InviteCommissionRecord.class)
                .eq(InviteCommissionRecord::getInviterUserId, inviterUserId)
                .orderBy(InviteCommissionRecord::getId, false);
        return recordMapper.paginate(pageNumber, pageSize, wrapper).map(this::toResp);
    }

    @Override
    public void settleVipUpgrade(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return;
        }
        try {
            VipUpgradeOrder order = findOrder(orderNo.trim());
            if (order == null || !ORDER_SUCCESS.equals(order.getStatus())) {
                return;
            }
            CommissionDraft draft = buildDraft(order);
            if (draft == null) {
                return;
            }
            InviteCommissionRecord record = createOrReuseRecord(draft);
            if (InviteCommissionStatus.SETTLED.name().equals(record.getStatus())) {
                return;
            }
            settleRecord(record);
        } catch (Exception e) {
            log.warn("邀请返佣处理失败，orderNo={}", orderNo, e);
        }
    }

    private CommissionDraft buildDraft(VipUpgradeOrder order) {
        if (order.getAmount() == null || order.getAmount().signum() <= 0) {
            return null;
        }
        UserInfo source = userInfoMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .eq(UserInfo::getUserId, order.getUserId())
                .limit(1));
        if (source == null || source.getInviter() == null || source.getInviter() <= 0) {
            return null;
        }
        VipLevelConfig config = vipLevelConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipLevelConfig.class)
                .eq(VipLevelConfig::getLevel, order.getToLevel())
                .limit(1));
        BigDecimal rate = config == null || config.getRebateRate() == null ? BigDecimal.ZERO : config.getRebateRate();
        if (rate.signum() <= 0) {
            return null;
        }
        BigDecimal commission = order.getAmount().multiply(rate).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
        if (commission.signum() <= 0) {
            return null;
        }
        return new CommissionDraft(
                source.getInviter(),
                source.getUserId(),
                order.getOrderNo(),
                normalizeCurrency(order.getCurrency()),
                order.getAmount().setScale(CURRENCY_SCALE, RoundingMode.UNNECESSARY),
                rate,
                commission);
    }

    private InviteCommissionRecord createOrReuseRecord(CommissionDraft draft) {
        InviteCommissionRecord exists = findBySourceOrder(draft.sourceOrderNo());
        if (exists != null) {
            return exists;
        }
        InviteCommissionRecord record = new InviteCommissionRecord();
        record.setRecordNo(nextRecordNo());
        record.setInviterUserId(draft.inviterUserId());
        record.setSourceUserId(draft.sourceUserId());
        record.setSourceOrderNo(draft.sourceOrderNo());
        record.setBusinessType(BUSINESS_VIP_UPGRADE);
        record.setCurrency(draft.currency());
        record.setSourceAmount(draft.sourceAmount());
        record.setCommissionRate(draft.commissionRate());
        record.setCommissionAmount(draft.commissionAmount());
        record.setStatus(InviteCommissionStatus.PENDING.name());
        try {
            recordMapper.insert(record);
            return record;
        } catch (DuplicateKeyException e) {
            InviteCommissionRecord duplicate = findBySourceOrder(draft.sourceOrderNo());
            if (duplicate != null) {
                return duplicate;
            }
            throw e;
        }
    }

    private void settleRecord(InviteCommissionRecord record) {
        try {
            WalletSettlementResp resp = apiRestClient.post(
                    serviceEndpoints.getWalletBaseUrl() + WALLET_SETTLEMENT_PATH,
                    walletReq(record),
                    WalletSettlementResp.class);
            markSettled(record.getId(), resp == null ? null : resp.getFlowNo());
        } catch (BizException e) {
            markSettleFailed(record.getId(), shortReason(e.getMessage()));
        } catch (Exception e) {
            markSettleFailed(record.getId(), shortReason(e.getMessage()));
        }
    }

    private void markSettled(Long id, String walletFlowNo) {
        InviteCommissionRecord record = recordMapper.selectOneById(id);
        if (record == null || InviteCommissionStatus.SETTLED.name().equals(record.getStatus())) {
            return;
        }
        record.setStatus(InviteCommissionStatus.SETTLED.name());
        record.setWalletFlowNo(walletFlowNo);
        record.setFailReason(null);
        record.setSettledAt(System.currentTimeMillis());
        recordMapper.update(record);
    }

    private void markSettleFailed(Long id, String reason) {
        InviteCommissionRecord record = recordMapper.selectOneById(id);
        if (record == null || InviteCommissionStatus.SETTLED.name().equals(record.getStatus())) {
            return;
        }
        record.setStatus(InviteCommissionStatus.SETTLE_FAILED.name());
        record.setFailReason(reason);
        recordMapper.update(record);
    }

    private WalletSettlementReq walletReq(InviteCommissionRecord record) {
        WalletSettlementReq req = new WalletSettlementReq();
        req.setUserId(record.getInviterUserId());
        req.setCurrency(record.getCurrency());
        req.setBizType(WALLET_BIZ_TYPE);
        req.setBizId(record.getSourceOrderNo());
        req.setAmount(record.getCommissionAmount());
        req.setRemark("邀请返佣：用户" + record.getSourceUserId() + " VIP升级");
        return req;
    }

    private InviteCommissionRecord findBySourceOrder(String sourceOrderNo) {
        return recordMapper.selectOneByQuery(QueryWrapper.create()
                .from(InviteCommissionRecord.class)
                .eq(InviteCommissionRecord::getBusinessType, BUSINESS_VIP_UPGRADE)
                .eq(InviteCommissionRecord::getSourceOrderNo, sourceOrderNo)
                .limit(1));
    }

    private VipUpgradeOrder findOrder(String orderNo) {
        return vipUpgradeOrderMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipUpgradeOrder.class)
                .eq(VipUpgradeOrder::getOrderNo, orderNo)
                .limit(1));
    }

    private InviteCommissionRecordResp toResp(InviteCommissionRecord r) {
        InviteCommissionRecordResp resp = new InviteCommissionRecordResp();
        resp.setId(r.getId());
        resp.setRecordNo(r.getRecordNo());
        resp.setInviterUserId(r.getInviterUserId());
        resp.setSourceUserId(r.getSourceUserId());
        resp.setSourceOrderNo(r.getSourceOrderNo());
        resp.setBusinessType(r.getBusinessType());
        resp.setCurrency(r.getCurrency());
        resp.setSourceAmount(r.getSourceAmount());
        resp.setCommissionRate(r.getCommissionRate());
        resp.setCommissionAmount(r.getCommissionAmount());
        resp.setStatus(r.getStatus());
        resp.setWalletFlowNo(r.getWalletFlowNo());
        resp.setFailReason(r.getFailReason());
        resp.setSettledAt(r.getSettledAt());
        resp.setCreateTime(r.getCreateTime());
        resp.setUpdateTime(r.getUpdateTime());
        return resp;
    }

    private String normalizeCurrency(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY;
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String shortReason(String value) {
        if (!StringUtils.hasText(value)) {
            return "钱包结算失败";
        }
        String text = value.trim();
        return text.length() <= 500 ? text : text.substring(0, 500);
    }

    private String nextRecordNo() {
        return "IC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private record CommissionDraft(
            Long inviterUserId,
            Long sourceUserId,
            String sourceOrderNo,
            String currency,
            BigDecimal sourceAmount,
            BigDecimal commissionRate,
            BigDecimal commissionAmount) {
    }
}