package com.mall.promotion.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.promotion.configuration.properties.ServiceEndpointProperties;
import com.mall.promotion.enums.PromotionCheckinRecordStatus;
import com.mall.promotion.enums.PromotionCouponStatus;
import com.mall.promotion.enums.PromotionRespCodeEnum;
import com.mall.promotion.mapper.PromotionCheckinRecordMapper;
import com.mall.promotion.mapper.PromotionCheckinRuleMapper;
import com.mall.promotion.mapper.PromotionCouponRecordMapper;
import com.mall.promotion.mapper.PromotionCouponTemplateMapper;
import com.mall.promotion.model.entity.PromotionCheckinRecord;
import com.mall.promotion.model.entity.PromotionCheckinRule;
import com.mall.promotion.model.entity.PromotionCouponRecord;
import com.mall.promotion.model.entity.PromotionCouponTemplate;
import com.mall.promotion.model.vo.PromotionCheckinRecordVO;
import com.mall.promotion.model.vo.PromotionCheckinRuleVO;
import com.mall.promotion.model.vo.PromotionCheckinStateVO;
import com.mall.promotion.model.vo.PromotionCouponRecordVO;
import com.mall.promotion.model.vo.PromotionCouponTemplateVO;
import com.mall.promotion.service.PromotionCouponService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 优惠券与签到：促销域持有业务事实，资金奖励只通过钱包结算 provider。 */
@Service
public class PromotionCouponServiceImpl implements PromotionCouponService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int DEFAULT_VALID_DAYS = 30;
    private static final int CURRENCY_SCALE = 6;
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final String WALLET_SETTLEMENT_PATH = "/api/provider/wallet/settlement/apply";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @Resource
    private PromotionCouponTemplateMapper couponTemplateMapper;

    @Resource
    private PromotionCouponRecordMapper couponRecordMapper;

    @Resource
    private PromotionCheckinRuleMapper checkinRuleMapper;

    @Resource
    private PromotionCheckinRecordMapper checkinRecordMapper;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public List<PromotionCouponTemplateVO> openTemplates(Long userId, Integer limit) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
        expireUserCoupons(userId);
        long now = System.currentTimeMillis();
        return couponTemplateMapper.selectListByQuery(QueryWrapper.create()
                        .from(PromotionCouponTemplate.class)
                        .eq(PromotionCouponTemplate::getStatus, STATUS_ENABLED)
                        .orderBy(PromotionCouponTemplate::getSortOrder, true)
                        .orderBy(PromotionCouponTemplate::getId, false)
                        .limit(safeLimit(limit)))
                .stream()
                .filter(t -> (t.getStartAt() == null || t.getStartAt() <= now)
                        && (t.getEndAt() == null || t.getEndAt() >= now))
                .map(t -> toTemplateVO(t, userId))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromotionCouponRecordVO claim(Long userId, Long templateId) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
        Preconditions.notNull(templateId, PromotionRespCodeEnum.COUPON_NOT_FOUND, "null");
        PromotionCouponTemplate template = couponTemplateMapper.selectOneById(templateId);
        Preconditions.notNull(template, PromotionRespCodeEnum.COUPON_NOT_FOUND, templateId);
        ensureCouponAvailable(template);

        long claimedCount = nz(couponRecordMapper.countClaimedByTemplate(userId, template.getId()));
        Preconditions.needTrue(claimedCount < Math.max(1, nz(template.getPerUserLimit())),
                PromotionRespCodeEnum.COUPON_LIMIT_REACHED);
        int totalStock = nz(template.getTotalStock());
        int claimedStock = nz(template.getClaimedStock());
        Preconditions.needTrue(totalStock == 0 || claimedStock < totalStock, PromotionRespCodeEnum.COUPON_STOCK_EMPTY);

        if (totalStock > 0) {
            template.setClaimedStock(claimedStock + 1);
            couponTemplateMapper.update(template);
        }

        PromotionCouponRecord record = new PromotionCouponRecord();
        long now = System.currentTimeMillis();
        record.setRecordNo(nextNo("CPN"));
        record.setUserId(userId);
        record.setTemplateId(template.getId());
        record.setCouponCode(template.getCouponCode());
        record.setTitle(template.getTitle());
        record.setCouponType(template.getCouponType());
        record.setCurrency(normalizeCurrency(template.getCurrency()));
        record.setDiscountAmount(normalizeAmount(template.getDiscountAmount()));
        record.setMinOrderAmount(normalizeAmount(template.getMinOrderAmount()));
        record.setStatus(PromotionCouponStatus.CLAIMED.name());
        record.setValidFrom(now);
        record.setValidTo(resolveValidTo(template, now));
        record.setClaimedAt(now);
        couponRecordMapper.insert(record);
        return toCouponRecordVO(record);
    }

    @Override
    public List<PromotionCouponRecordVO> userCoupons(Long userId, String status, Integer limit) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
        expireUserCoupons(userId);
        String normalizedStatus = normalizeStatus(status);
        return couponRecordMapper.selectListByQuery(QueryWrapper.create()
                        .from(PromotionCouponRecord.class)
                        .eq(PromotionCouponRecord::getUserId, userId)
                        .eq(PromotionCouponRecord::getStatus, normalizedStatus, StringUtils.hasText(normalizedStatus))
                        .orderBy(PromotionCouponRecord::getId, false)
                        .limit(safeLimit(limit)))
                .stream()
                .map(this::toCouponRecordVO)
                .toList();
    }

    @Override
    public PromotionCheckinStateVO checkinState(Long userId, Integer limit) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.CHECKIN_RULE_NOT_FOUND);
        int today = todayDate();
        PromotionCheckinRecord todayRecord = checkinRecordMapper.selectByUserAndDate(userId, today);
        PromotionCheckinRecord latestBefore = checkinRecordMapper.selectLatestBefore(userId, today);
        int currentConsecutive = todayRecord != null
                ? nz(todayRecord.getConsecutiveDays())
                : consecutiveFromLatestBefore(latestBefore, today);
        int nextConsecutive = todayRecord != null ? currentConsecutive : currentConsecutive + 1;

        PromotionCheckinStateVO vo = new PromotionCheckinStateVO();
        vo.setCheckedToday(todayRecord != null);
        vo.setTodayDate(today);
        vo.setConsecutiveDays(currentConsecutive);
        vo.setTodayRecord(todayRecord == null ? null : toCheckinRecordVO(todayRecord));
        vo.setTodayRule(toCheckinRuleVO(resolveRule(nextConsecutive)));
        vo.setRules(enabledRules().stream().map(this::toCheckinRuleVO).toList());
        vo.setRecentRecords(checkinRecords(userId, limit));
        return vo;
    }

    @Override
    public PromotionCheckinRecordVO checkin(Long userId) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.CHECKIN_RULE_NOT_FOUND);
        int today = todayDate();
        Preconditions.needTrue(checkinRecordMapper.selectByUserAndDate(userId, today) == null,
                PromotionRespCodeEnum.CHECKIN_ALREADY_DONE);
        PromotionCheckinRecord record = createCheckinRecord(userId, today);
        return toCheckinRecordVO(settleCheckinReward(record.getId()));
    }

    @Override
    public List<PromotionCheckinRecordVO> checkinRecords(Long userId, Integer limit) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.CHECKIN_RULE_NOT_FOUND);
        return checkinRecordMapper.selectListByQuery(QueryWrapper.create()
                        .from(PromotionCheckinRecord.class)
                        .eq(PromotionCheckinRecord::getUserId, userId)
                        .orderBy(PromotionCheckinRecord::getCheckinDate, false)
                        .limit(safeLimit(limit)))
                .stream()
                .map(this::toCheckinRecordVO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    protected PromotionCheckinRecord createCheckinRecord(Long userId, int today) throws BizException {
        PromotionCheckinRecord previous = checkinRecordMapper.selectLatestBefore(userId, today);
        int consecutiveDays = consecutiveFromLatestBefore(previous, today) + 1;
        PromotionCheckinRule rule = resolveRule(consecutiveDays);
        PromotionCheckinRecord record = new PromotionCheckinRecord();
        record.setRecordNo(nextNo("CHK"));
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setConsecutiveDays(consecutiveDays);
        record.setCurrency(normalizeCurrency(rule.getCurrency()));
        record.setRewardAmount(normalizeAmount(rule.getRewardAmount()));
        record.setStatus(PromotionCheckinRecordStatus.SETTLE_FAILED.name());
        record.setCheckedAt(System.currentTimeMillis());
        checkinRecordMapper.insert(record);
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    protected PromotionCheckinRecord markCheckinSettled(PromotionCheckinRecord source, String walletFlowNo, String failReason) {
        PromotionCheckinRecord record = checkinRecordMapper.selectOneById(source.getId());
        record.setStatus(PromotionCheckinRecordStatus.SETTLED.name());
        record.setWalletFlowNo(walletFlowNo);
        record.setFailReason(failReason);
        record.setSettledAt(System.currentTimeMillis());
        checkinRecordMapper.update(record);
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    protected PromotionCheckinRecord markCheckinFailed(PromotionCheckinRecord source, String failReason) {
        PromotionCheckinRecord record = checkinRecordMapper.selectOneById(source.getId());
        record.setStatus(PromotionCheckinRecordStatus.SETTLE_FAILED.name());
        record.setFailReason(shortReason(failReason));
        checkinRecordMapper.update(record);
        return record;
    }

    private PromotionCheckinRecord settleCheckinReward(Long recordId) throws BizException {
        PromotionCheckinRecord record = checkinRecordMapper.selectOneById(recordId);
        Preconditions.notNull(record, PromotionRespCodeEnum.CHECKIN_RECORD_NOT_FOUND, recordId);
        if (normalizeAmount(record.getRewardAmount()).signum() <= 0) {
            return markCheckinSettled(record, null, null);
        }
        try {
            WalletSettlementResp walletResp = apiRestClient.post(
                    serviceEndpoints.getWalletBaseUrl() + WALLET_SETTLEMENT_PATH,
                    walletReq(record),
                    WalletSettlementResp.class);
            return markCheckinSettled(record, walletResp == null ? null : walletResp.getFlowNo(), null);
        } catch (BizException e) {
            return markCheckinFailed(record, e.getMessage());
        } catch (Exception e) {
            return markCheckinFailed(record, e.getMessage());
        }
    }

    private WalletSettlementReq walletReq(PromotionCheckinRecord record) {
        WalletSettlementReq req = new WalletSettlementReq();
        req.setUserId(record.getUserId());
        req.setCurrency(record.getCurrency());
        req.setBizType("CHECKIN_REWARD");
        req.setBizId(record.getRecordNo());
        req.setAmount(record.getRewardAmount());
        req.setRemark("签到奖励：连续" + record.getConsecutiveDays() + "天");
        return req;
    }

    private void ensureCouponAvailable(PromotionCouponTemplate template) throws BizException {
        long now = System.currentTimeMillis();
        Preconditions.needTrue(template.getStatus() != null && template.getStatus() == STATUS_ENABLED,
                PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
        Preconditions.needTrue(template.getStartAt() == null || template.getStartAt() <= now,
                PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
        Preconditions.needTrue(template.getEndAt() == null || template.getEndAt() >= now,
                PromotionRespCodeEnum.COUPON_NOT_AVAILABLE);
    }

    private void expireUserCoupons(Long userId) {
        long now = System.currentTimeMillis();
        couponRecordMapper.selectListByQuery(QueryWrapper.create()
                        .from(PromotionCouponRecord.class)
                        .eq(PromotionCouponRecord::getUserId, userId)
                        .eq(PromotionCouponRecord::getStatus, PromotionCouponStatus.CLAIMED.name())
                        .lt(PromotionCouponRecord::getValidTo, now))
                .forEach(record -> {
                    record.setStatus(PromotionCouponStatus.EXPIRED.name());
                    record.setExpiredAt(now);
                    couponRecordMapper.update(record);
                });
    }

    private Long resolveValidTo(PromotionCouponTemplate template, long now) {
        int validDays = nz(template.getValidDays()) > 0 ? template.getValidDays() : DEFAULT_VALID_DAYS;
        long byDays = now + validDays * 86_400_000L;
        return template.getEndAt() == null ? byDays : Math.min(byDays, template.getEndAt());
    }

    private List<PromotionCheckinRule> enabledRules() {
        return checkinRuleMapper.selectListByQuery(QueryWrapper.create()
                .from(PromotionCheckinRule.class)
                .eq(PromotionCheckinRule::getStatus, STATUS_ENABLED)
                .orderBy(PromotionCheckinRule::getRequiredConsecutiveDays, true)
                .orderBy(PromotionCheckinRule::getSortOrder, true));
    }

    private PromotionCheckinRule resolveRule(int consecutiveDays) throws BizException {
        List<PromotionCheckinRule> rules = enabledRules();
        Preconditions.needTrue(!rules.isEmpty(), PromotionRespCodeEnum.CHECKIN_RULE_NOT_FOUND);
        return rules.stream()
                .filter(r -> nz(r.getRequiredConsecutiveDays()) == consecutiveDays)
                .findFirst()
                .or(() -> rules.stream().filter(r -> nz(r.getRequiredConsecutiveDays()) == 1).findFirst())
                .orElse(rules.get(0));
    }

    private int consecutiveFromLatestBefore(PromotionCheckinRecord latestBefore, int today) {
        if (latestBefore == null) {
            return 0;
        }
        return latestBefore.getCheckinDate() != null && latestBefore.getCheckinDate() == previousDate(today)
                ? nz(latestBefore.getConsecutiveDays())
                : 0;
    }

    private int todayDate() {
        return dateToInt(LocalDate.now(ZONE_ID));
    }

    private int previousDate(int date) {
        return dateToInt(LocalDate.parse(String.valueOf(date), DATE_FORMAT).minusDays(1));
    }

    private int dateToInt(LocalDate date) {
        return Integer.parseInt(date.format(DATE_FORMAT));
    }

    private PromotionCouponTemplateVO toTemplateVO(PromotionCouponTemplate t, Long userId) {
        PromotionCouponTemplateVO vo = new PromotionCouponTemplateVO();
        vo.setId(t.getId());
        vo.setCouponCode(t.getCouponCode());
        vo.setTitle(t.getTitle());
        vo.setDescription(t.getDescription());
        vo.setCouponType(t.getCouponType());
        vo.setCurrency(t.getCurrency());
        vo.setDiscountAmount(t.getDiscountAmount());
        vo.setMinOrderAmount(t.getMinOrderAmount());
        vo.setTotalStock(t.getTotalStock());
        vo.setClaimedStock(t.getClaimedStock());
        vo.setRemainStock(nz(t.getTotalStock()) == 0 ? -1 : Math.max(0, nz(t.getTotalStock()) - nz(t.getClaimedStock())));
        vo.setPerUserLimit(t.getPerUserLimit());
        vo.setUserClaimedCount((int) nz(couponRecordMapper.countClaimedByTemplate(userId, t.getId())));
        vo.setValidDays(t.getValidDays());
        vo.setStartAt(t.getStartAt());
        vo.setEndAt(t.getEndAt());
        vo.setStatus(t.getStatus());
        return vo;
    }

    private PromotionCouponRecordVO toCouponRecordVO(PromotionCouponRecord r) {
        PromotionCouponRecordVO vo = new PromotionCouponRecordVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setTemplateId(r.getTemplateId());
        vo.setCouponCode(r.getCouponCode());
        vo.setTitle(r.getTitle());
        vo.setCouponType(r.getCouponType());
        vo.setCurrency(r.getCurrency());
        vo.setDiscountAmount(r.getDiscountAmount());
        vo.setMinOrderAmount(r.getMinOrderAmount());
        vo.setStatus(r.getStatus());
        vo.setValidFrom(r.getValidFrom());
        vo.setValidTo(r.getValidTo());
        vo.setClaimedAt(r.getClaimedAt());
        vo.setLockedAt(r.getLockedAt());
        vo.setUsedAt(r.getUsedAt());
        vo.setExpiredAt(r.getExpiredAt());
        return vo;
    }

    private PromotionCheckinRuleVO toCheckinRuleVO(PromotionCheckinRule r) {
        PromotionCheckinRuleVO vo = new PromotionCheckinRuleVO();
        vo.setId(r.getId());
        vo.setRuleCode(r.getRuleCode());
        vo.setTitle(r.getTitle());
        vo.setRequiredConsecutiveDays(r.getRequiredConsecutiveDays());
        vo.setCurrency(r.getCurrency());
        vo.setRewardAmount(r.getRewardAmount());
        vo.setStatus(r.getStatus());
        return vo;
    }

    private PromotionCheckinRecordVO toCheckinRecordVO(PromotionCheckinRecord r) {
        PromotionCheckinRecordVO vo = new PromotionCheckinRecordVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setCheckinDate(r.getCheckinDate());
        vo.setConsecutiveDays(r.getConsecutiveDays());
        vo.setCurrency(r.getCurrency());
        vo.setRewardAmount(r.getRewardAmount());
        vo.setStatus(r.getStatus());
        vo.setWalletFlowNo(r.getWalletFlowNo());
        vo.setFailReason(r.getFailReason());
        vo.setCheckedAt(r.getCheckedAt());
        vo.setSettledAt(r.getSettledAt());
        return vo;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrency(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) throws BizException {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        Preconditions.needTrue(value.signum() >= 0, PromotionRespCodeEnum.COUPON_INVALID);
        Preconditions.needTrue(value.scale() <= CURRENCY_SCALE, PromotionRespCodeEnum.COUPON_INVALID);
        return value.setScale(CURRENCY_SCALE, RoundingMode.UNNECESSARY);
    }

    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String nextNo(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        return prefix + "-" + Instant.now().toEpochMilli() + "-" + random;
    }

    private String shortReason(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private long nz(Long value) {
        return value == null ? 0L : value;
    }
}