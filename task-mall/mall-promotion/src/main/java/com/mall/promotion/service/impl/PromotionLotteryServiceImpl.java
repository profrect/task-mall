package com.mall.promotion.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.PromotionLotteryActivityQueryReq;
import com.mall.common.model.dto.req.PromotionLotteryActivityReq;
import com.mall.common.model.dto.req.PromotionLotteryPrizeReq;
import com.mall.common.model.dto.req.PromotionLotteryRecordQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeQueryReq;
import com.mall.common.model.dto.req.PromotionPrizeReq;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.PromotionLotteryActivityResp;
import com.mall.common.model.dto.resp.PromotionLotteryDetailResp;
import com.mall.common.model.dto.resp.PromotionLotteryPrizeResp;
import com.mall.common.model.dto.resp.PromotionLotteryRecordResp;
import com.mall.common.model.dto.resp.PromotionPrizeResp;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.promotion.configuration.properties.ServiceEndpointProperties;
import com.mall.promotion.enums.PromotionLotteryRecordStatus;
import com.mall.promotion.enums.PromotionPrizeType;
import com.mall.promotion.enums.PromotionRespCodeEnum;
import com.mall.promotion.mapper.PromotionLotteryActivityMapper;
import com.mall.promotion.mapper.PromotionLotteryPrizeMapper;
import com.mall.promotion.mapper.PromotionLotteryRecordMapper;
import com.mall.promotion.mapper.PromotionPrizeMapper;
import com.mall.promotion.model.entity.PromotionLotteryActivity;
import com.mall.promotion.model.entity.PromotionLotteryPrize;
import com.mall.promotion.model.entity.PromotionLotteryRecord;
import com.mall.promotion.model.entity.PromotionPrize;
import com.mall.promotion.service.PromotionLotteryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 抽奖状态机：促销服务持有活动/奖品/抽奖记录事实；现金奖通过钱包 provider 入账。
 */
@Service
public class PromotionLotteryServiceImpl implements PromotionLotteryService {

    private static final int STATUS_ENABLED = 1;
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final int CURRENCY_SCALE = 6;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final String WALLET_SETTLEMENT_PATH = "/api/provider/wallet/settlement/apply";

    @Resource
    private PromotionPrizeMapper prizeMapper;

    @Resource
    private PromotionLotteryActivityMapper activityMapper;

    @Resource
    private PromotionLotteryPrizeMapper lotteryPrizeMapper;

    @Resource
    private PromotionLotteryRecordMapper recordMapper;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Page<PromotionPrizeResp> prizePage(PromotionPrizeQueryReq req) throws BizException {
        PromotionPrizeQueryReq q = req == null ? new PromotionPrizeQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(PromotionPrize.class)
                .eq(PromotionPrize::getPrizeType, normalizeUpper(q.getPrizeType()), StringUtils.hasText(q.getPrizeType()))
                .eq(PromotionPrize::getStatus, q.getStatus(), q.getStatus() != null)
                .and(w -> w.like(PromotionPrize::getPrizeName, q.getKeyword())
                        .or(PromotionPrize::getPrizeCode).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(PromotionPrize::getSortOrder, true)
                .orderBy(PromotionPrize::getId, false);
        return prizeMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper).map(this::toPrizeResp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PromotionPrizeResp savePrize(PromotionPrizeReq req) throws BizException {
        Preconditions.notNull(req, PromotionRespCodeEnum.PRIZE_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getPrizeName()), PromotionRespCodeEnum.PRIZE_INVALID);
        PromotionPrize item = req.getId() == null ? new PromotionPrize() : prizeMapper.selectOneById(req.getId());
        Preconditions.notNull(item, PromotionRespCodeEnum.PRIZE_NOT_FOUND, req.getId());
        fillPrize(item, req);
        if (item.getId() == null) {
            prizeMapper.insert(item);
        } else {
            prizeMapper.update(item);
        }
        return toPrizeResp(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePrize(Long id) throws BizException {
        PromotionPrize item = prizeMapper.selectOneById(id);
        Preconditions.notNull(item, PromotionRespCodeEnum.PRIZE_NOT_FOUND, id);
        prizeMapper.deleteById(id);
    }

    @Override
    public Page<PromotionLotteryActivityResp> activityPage(PromotionLotteryActivityQueryReq req) throws BizException {
        PromotionLotteryActivityQueryReq q = req == null ? new PromotionLotteryActivityQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(PromotionLotteryActivity.class)
                .eq(PromotionLotteryActivity::getStatus, q.getStatus(), q.getStatus() != null)
                .and(w -> w.like(PromotionLotteryActivity::getTitle, q.getKeyword())
                        .or(PromotionLotteryActivity::getActivityCode).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(PromotionLotteryActivity::getSortOrder, true)
                .orderBy(PromotionLotteryActivity::getId, false);
        return activityMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper).map(this::toActivityResp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PromotionLotteryActivityResp saveActivity(PromotionLotteryActivityReq req) throws BizException {
        Preconditions.notNull(req, PromotionRespCodeEnum.ACTIVITY_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getTitle()), PromotionRespCodeEnum.ACTIVITY_INVALID);
        PromotionLotteryActivity item = req.getId() == null
                ? new PromotionLotteryActivity()
                : activityMapper.selectOneById(req.getId());
        Preconditions.notNull(item, PromotionRespCodeEnum.ACTIVITY_NOT_FOUND, req.getId());
        fillActivity(item, req);
        if (item.getId() == null) {
            activityMapper.insert(item);
        } else {
            activityMapper.update(item);
        }
        return toActivityResp(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteActivity(Long id) throws BizException {
        PromotionLotteryActivity item = activityMapper.selectOneById(id);
        Preconditions.notNull(item, PromotionRespCodeEnum.ACTIVITY_NOT_FOUND, id);
        activityMapper.deleteById(id);
    }

    @Override
    public List<PromotionLotteryPrizeResp> activityPrizes(Long activityId) throws BizException {
        Preconditions.notNull(activityId, PromotionRespCodeEnum.ACTIVITY_NOT_FOUND, "null");
        return lotteryPrizeMapper.listByActivity(activityId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PromotionLotteryPrizeResp saveActivityPrize(PromotionLotteryPrizeReq req) throws BizException {
        Preconditions.notNull(req, PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        Preconditions.notNull(req.getActivityId(), PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        Preconditions.notNull(req.getPrizeId(), PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        Preconditions.needTrue(nz(req.getWeight()) > 0, PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        Preconditions.notNull(activityMapper.selectOneById(req.getActivityId()),
                PromotionRespCodeEnum.ACTIVITY_NOT_FOUND, req.getActivityId());
        Preconditions.notNull(prizeMapper.selectOneById(req.getPrizeId()),
                PromotionRespCodeEnum.PRIZE_NOT_FOUND, req.getPrizeId());
        PromotionLotteryPrize item = req.getId() == null
                ? new PromotionLotteryPrize()
                : lotteryPrizeMapper.selectOneById(req.getId());
        Preconditions.notNull(item, PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        item.setActivityId(req.getActivityId());
        item.setPrizeId(req.getPrizeId());
        item.setWeight(req.getWeight());
        item.setDailyLimit(nz(req.getDailyLimit()));
        item.setSortOrder(nz(req.getSortOrder()));
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
        if (item.getId() == null) {
            lotteryPrizeMapper.insert(item);
        } else {
            lotteryPrizeMapper.update(item);
        }
        return lotteryPrizeMapper.listByActivity(item.getActivityId()).stream()
                .filter(p -> item.getId().equals(p.getId()))
                .findFirst()
                .orElseThrow(BizException::new);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteActivityPrize(Long id) throws BizException {
        PromotionLotteryPrize item = lotteryPrizeMapper.selectOneById(id);
        Preconditions.notNull(item, PromotionRespCodeEnum.LOTTERY_PRIZE_INVALID);
        lotteryPrizeMapper.deleteById(id);
    }

    @Override
    public Page<PromotionLotteryRecordResp> recordPage(PromotionLotteryRecordQueryReq req) throws BizException {
        PromotionLotteryRecordQueryReq q = req == null ? new PromotionLotteryRecordQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(PromotionLotteryRecord.class)
                .eq(PromotionLotteryRecord::getUserId, q.getUserId(), q.getUserId() != null)
                .eq(PromotionLotteryRecord::getActivityId, q.getActivityId(), q.getActivityId() != null)
                .eq(PromotionLotteryRecord::getStatus, normalizeUpper(q.getStatus()), StringUtils.hasText(q.getStatus()))
                .and(w -> w.like(PromotionLotteryRecord::getRecordNo, q.getKeyword())
                        .or(PromotionLotteryRecord::getPrizeName).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(PromotionLotteryRecord::getId, false);
        return recordMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper).map(this::toRecordResp);
    }

    @Override
    public List<PromotionLotteryDetailResp> openActivities(Long userId) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
        long now = System.currentTimeMillis();
        List<PromotionLotteryActivity> rows = activityMapper.listOpenActivities(now);
        long dayStart = dayStart(now);
        return rows.stream().map(a -> toDetailResp(userId, a, dayStart)).toList();
    }

    @Override
    public PromotionLotteryRecordResp draw(Long userId, Long activityId) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
        PromotionLotteryRecord record = createDrawRecord(userId, activityId);
        PromotionLotteryRecord settled = settleCashPrize(record.getId());
        return toRecordResp(settled);
    }

    @Override
    public List<PromotionLotteryRecordResp> userRecords(Long userId, Integer limit) throws BizException {
        Preconditions.notNull(userId, PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
        return recordMapper.selectListByQuery(QueryWrapper.create()
                        .from(PromotionLotteryRecord.class)
                        .eq(PromotionLotteryRecord::getUserId, userId)
                        .orderBy(PromotionLotteryRecord::getId, false)
                        .limit(safeLimit(limit)))
                .stream()
                .map(this::toRecordResp)
                .toList();
    }

    private PromotionLotteryRecord createDrawRecord(Long userId, Long activityId) throws BizException {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    PromotionLotteryActivity activity = activityMapper.selectOneById(activityId);
                    Preconditions.notNull(activity, PromotionRespCodeEnum.ACTIVITY_NOT_FOUND, activityId);
                    ensureActivityAvailable(activity);
                    long now = System.currentTimeMillis();
                    long dayStart = dayStart(now);
                    long todayCount = nz(recordMapper.countToday(userId, activity.getId(), dayStart));
                    Preconditions.needTrue(nz(activity.getDailyLimit()) == 0 || todayCount < activity.getDailyLimit(),
                            PromotionRespCodeEnum.LOTTERY_LIMIT_REACHED);

                    List<PromotionLotteryPrizeResp> pool = lotteryPrizeMapper.listDrawableByActivity(activity.getId()).stream()
                            .filter(p -> withinPrizeDailyLimit(p, dayStart))
                            .toList();
                    Preconditions.needTrue(!pool.isEmpty(), PromotionRespCodeEnum.LOTTERY_POOL_EMPTY);
                    PromotionLotteryPrizeResp chosen = chooseByWeight(pool);
                    PromotionPrize prize = prizeMapper.selectOneById(chosen.getPrizeId());
                    Preconditions.notNull(prize, PromotionRespCodeEnum.PRIZE_NOT_FOUND, chosen.getPrizeId());
                    consumeStock(prize);

                    PromotionLotteryRecord record = new PromotionLotteryRecord();
                    record.setRecordNo(nextRecordNo());
                    record.setUserId(userId);
                    record.setActivityId(activity.getId());
                    record.setActivityTitle(activity.getTitle());
                    record.setPrizeId(prize.getId());
                    record.setPrizeCode(prize.getPrizeCode());
                    record.setPrizeName(prize.getPrizeName());
                    record.setPrizeType(prize.getPrizeType());
                    record.setCurrency(normalizeCurrency(prize.getCurrency()));
                    record.setAmount(normalizeAmount(prize.getAmount()));
                    record.setStatus(PromotionLotteryRecordStatus.DRAWN.name());
                    record.setDrawnAt(now);
                    recordMapper.insert(record);
                    return record;
                } catch (BizException e) {
                    throw new PromotionBizRuntimeException(e);
                }
            });
        } catch (PromotionBizRuntimeException e) {
            throw e.getBizException();
        }
    }

    private PromotionLotteryRecord settleCashPrize(Long recordId) throws BizException {
        PromotionLotteryRecord record = recordMapper.selectOneById(recordId);
        Preconditions.notNull(record, PromotionRespCodeEnum.LOTTERY_RECORD_NOT_FOUND, recordId);
        if (!PromotionPrizeType.CASH.name().equals(record.getPrizeType())) {
            return markSettled(record, null, null);
        }
        try {
            WalletSettlementResp walletResp = apiRestClient.post(
                    serviceEndpoints.getWalletBaseUrl() + WALLET_SETTLEMENT_PATH,
                    walletReq(record),
                    WalletSettlementResp.class);
            return markSettled(record, walletResp == null ? null : walletResp.getFlowNo(), null);
        } catch (BizException e) {
            return markSettleFailed(record, shortReason(e.getMessage()));
        } catch (Exception e) {
            return markSettleFailed(record, shortReason(e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected PromotionLotteryRecord markSettled(PromotionLotteryRecord source, String walletFlowNo, String failReason) {
        PromotionLotteryRecord record = recordMapper.selectOneById(source.getId());
        record.setStatus(PromotionLotteryRecordStatus.SETTLED.name());
        record.setWalletFlowNo(walletFlowNo);
        record.setFailReason(failReason);
        record.setSettledAt(System.currentTimeMillis());
        recordMapper.update(record);
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    protected PromotionLotteryRecord markSettleFailed(PromotionLotteryRecord source, String reason) {
        PromotionLotteryRecord record = recordMapper.selectOneById(source.getId());
        record.setStatus(PromotionLotteryRecordStatus.SETTLE_FAILED.name());
        record.setFailReason(reason);
        recordMapper.update(record);
        return record;
    }

    private void fillPrize(PromotionPrize item, PromotionPrizeReq req) throws BizException {
        String type = normalizeUpper(req.getPrizeType());
        if (!StringUtils.hasText(type)) {
            type = PromotionPrizeType.CASH.name();
        }
        parsePrizeType(type);
        item.setPrizeCode(StringUtils.hasText(req.getPrizeCode()) ? normalizeCode(req.getPrizeCode()) : defaultCode("PRIZE"));
        item.setPrizeName(req.getPrizeName().trim());
        item.setPrizeType(type);
        item.setCurrency(normalizeCurrency(req.getCurrency()));
        item.setAmount(normalizeAmount(req.getAmount()));
        Preconditions.needTrue(!PromotionPrizeType.CASH.name().equals(type) || item.getAmount().signum() > 0,
                PromotionRespCodeEnum.PRIZE_INVALID);
        item.setStockTotal(nz(req.getStockTotal()));
        item.setStockUsed(nz(req.getStockUsed()));
        Preconditions.needTrue(item.getStockTotal() == 0 || item.getStockUsed() <= item.getStockTotal(),
                PromotionRespCodeEnum.PRIZE_INVALID);
        item.setSortOrder(nz(req.getSortOrder()));
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
        item.setRemark(req.getRemark());
    }

    private void fillActivity(PromotionLotteryActivity item, PromotionLotteryActivityReq req) throws BizException {
        item.setActivityCode(StringUtils.hasText(req.getActivityCode())
                ? normalizeCode(req.getActivityCode())
                : defaultCode("LOTTERY"));
        item.setTitle(req.getTitle().trim());
        item.setDescription(req.getDescription());
        item.setDailyLimit(nz(req.getDailyLimit()));
        item.setStartAt(req.getStartAt());
        item.setEndAt(req.getEndAt());
        Preconditions.needTrue(req.getStartAt() == null || req.getEndAt() == null || req.getStartAt() <= req.getEndAt(),
                PromotionRespCodeEnum.ACTIVITY_INVALID);
        item.setSortOrder(nz(req.getSortOrder()));
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
    }

    private void ensureActivityAvailable(PromotionLotteryActivity activity) throws BizException {
        long now = System.currentTimeMillis();
        Preconditions.needTrue(activity.getStatus() != null && activity.getStatus() == STATUS_ENABLED,
                PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
        Preconditions.needTrue(activity.getStartAt() == null || activity.getStartAt() <= now,
                PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
        Preconditions.needTrue(activity.getEndAt() == null || activity.getEndAt() >= now,
                PromotionRespCodeEnum.ACTIVITY_NOT_AVAILABLE);
    }

    private boolean withinPrizeDailyLimit(PromotionLotteryPrizeResp p, long dayStart) {
        int limit = nz(p.getDailyLimit());
        if (limit <= 0) {
            return true;
        }
        return nz(recordMapper.countPrizeToday(p.getPrizeId(), dayStart)) < limit;
    }

    private PromotionLotteryPrizeResp chooseByWeight(List<PromotionLotteryPrizeResp> pool) throws BizException {
        int total = pool.stream().mapToInt(p -> Math.max(0, nz(p.getWeight()))).sum();
        Preconditions.needTrue(total > 0, PromotionRespCodeEnum.LOTTERY_POOL_EMPTY);
        int point = ThreadLocalRandom.current().nextInt(total);
        int cursor = 0;
        for (PromotionLotteryPrizeResp item : pool) {
            cursor += Math.max(0, nz(item.getWeight()));
            if (point < cursor) {
                return item;
            }
        }
        return pool.get(pool.size() - 1);
    }

    private void consumeStock(PromotionPrize prize) throws BizException {
        int total = nz(prize.getStockTotal());
        int used = nz(prize.getStockUsed());
        if (total > 0) {
            Preconditions.needTrue(used < total, PromotionRespCodeEnum.PRIZE_STOCK_EMPTY);
            prize.setStockUsed(used + 1);
            prizeMapper.update(prize);
        }
    }

    private PromotionLotteryDetailResp toDetailResp(Long userId, PromotionLotteryActivity activity, long dayStart) {
        int todayCount = nz(recordMapper.countToday(userId, activity.getId(), dayStart)).intValue();
        int dailyLimit = nz(activity.getDailyLimit());
        PromotionLotteryDetailResp resp = new PromotionLotteryDetailResp();
        resp.setActivity(toActivityResp(activity));
        resp.setPrizes(lotteryPrizeMapper.listDrawableByActivity(activity.getId()));
        resp.setTodayDrawCount(todayCount);
        resp.setTodayRemainCount(dailyLimit == 0 ? -1 : Math.max(0, dailyLimit - todayCount));
        return resp;
    }

    private WalletSettlementReq walletReq(PromotionLotteryRecord record) {
        WalletSettlementReq req = new WalletSettlementReq();
        req.setUserId(record.getUserId());
        req.setCurrency(record.getCurrency());
        req.setBizType("LOTTERY_REWARD");
        req.setBizId(record.getRecordNo());
        req.setAmount(record.getAmount());
        req.setRemark("抽奖奖励：" + record.getPrizeName());
        return req;
    }

    private PromotionPrizeResp toPrizeResp(PromotionPrize p) {
        PromotionPrizeResp resp = new PromotionPrizeResp();
        resp.setId(p.getId());
        resp.setPrizeCode(p.getPrizeCode());
        resp.setPrizeName(p.getPrizeName());
        resp.setPrizeType(p.getPrizeType());
        resp.setCurrency(p.getCurrency());
        resp.setAmount(p.getAmount());
        resp.setStockTotal(p.getStockTotal());
        resp.setStockUsed(p.getStockUsed());
        resp.setSortOrder(p.getSortOrder());
        resp.setStatus(p.getStatus());
        resp.setRemark(p.getRemark());
        resp.setCreateTime(p.getCreateTime());
        resp.setUpdateTime(p.getUpdateTime());
        return resp;
    }

    private PromotionLotteryActivityResp toActivityResp(PromotionLotteryActivity a) {
        PromotionLotteryActivityResp resp = new PromotionLotteryActivityResp();
        resp.setId(a.getId());
        resp.setActivityCode(a.getActivityCode());
        resp.setTitle(a.getTitle());
        resp.setDescription(a.getDescription());
        resp.setDailyLimit(a.getDailyLimit());
        resp.setStartAt(a.getStartAt());
        resp.setEndAt(a.getEndAt());
        resp.setSortOrder(a.getSortOrder());
        resp.setStatus(a.getStatus());
        resp.setCreateTime(a.getCreateTime());
        resp.setUpdateTime(a.getUpdateTime());
        return resp;
    }

    private PromotionLotteryRecordResp toRecordResp(PromotionLotteryRecord r) {
        PromotionLotteryRecordResp resp = new PromotionLotteryRecordResp();
        resp.setId(r.getId());
        resp.setRecordNo(r.getRecordNo());
        resp.setUserId(r.getUserId());
        resp.setActivityId(r.getActivityId());
        resp.setActivityTitle(r.getActivityTitle());
        resp.setPrizeId(r.getPrizeId());
        resp.setPrizeCode(r.getPrizeCode());
        resp.setPrizeName(r.getPrizeName());
        resp.setPrizeType(r.getPrizeType());
        resp.setCurrency(r.getCurrency());
        resp.setAmount(r.getAmount());
        resp.setStatus(r.getStatus());
        resp.setWalletFlowNo(r.getWalletFlowNo());
        resp.setFailReason(r.getFailReason());
        resp.setDrawnAt(r.getDrawnAt());
        resp.setSettledAt(r.getSettledAt());
        resp.setCreateTime(r.getCreateTime());
        resp.setUpdateTime(r.getUpdateTime());
        return resp;
    }

    private PromotionPrizeType parsePrizeType(String value) throws BizException {
        try {
            return PromotionPrizeType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BizException(PromotionRespCodeEnum.PRIZE_INVALID, new Object[]{value});
        }
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_-]", "_");
    }

    private String normalizeCurrency(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) throws BizException {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        Preconditions.needTrue(value.signum() >= 0, PromotionRespCodeEnum.PRIZE_INVALID);
        Preconditions.needTrue(value.scale() <= CURRENCY_SCALE, PromotionRespCodeEnum.PRIZE_INVALID);
        return value.setScale(CURRENCY_SCALE, RoundingMode.UNNECESSARY);
    }

    private String shortReason(String value) {
        if (!StringUtils.hasText(value)) {
            return "钱包结算失败";
        }
        String text = value.trim();
        return text.length() <= 500 ? text : text.substring(0, 500);
    }

    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private Long nz(Long value) {
        return value == null ? 0L : value;
    }

    private String defaultCode(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private String nextRecordNo() {
        return "LR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private long dayStart(long now) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private static class PromotionBizRuntimeException extends RuntimeException {

        private final BizException bizException;

        PromotionBizRuntimeException(BizException bizException) {
            super(bizException);
            this.bizException = bizException;
        }

        BizException getBizException() {
            return bizException;
        }
    }
}