package com.mall.mission.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.MissionGoodsQueryReq;
import com.mall.common.model.dto.req.MissionGoodsReq;
import com.mall.common.model.dto.req.MissionTaskQueryReq;
import com.mall.common.model.dto.req.MissionTaskReq;
import com.mall.common.model.dto.req.MissionTaskReviewReq;
import com.mall.common.model.dto.req.MissionUserTaskQueryReq;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.LeaderboardItemResp;
import com.mall.common.model.dto.resp.MissionGoodsResp;
import com.mall.common.model.dto.resp.MissionTaskResp;
import com.mall.common.model.dto.resp.MissionUserTaskResp;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.mission.configuration.properties.ServiceEndpointProperties;
import com.mall.mission.enums.MissionGoodsType;
import com.mall.mission.enums.MissionRespCodeEnum;
import com.mall.mission.enums.MissionRewardSettlementStatus;
import com.mall.mission.enums.MissionUserTaskStatus;
import com.mall.mission.mapper.MissionGoodsMapper;
import com.mall.mission.mapper.MissionRewardSettlementMapper;
import com.mall.mission.mapper.MissionTaskMapper;
import com.mall.mission.mapper.MissionUserTaskMapper;
import com.mall.mission.model.dto.MissionSubmitDTO;
import com.mall.mission.model.entity.MissionGoods;
import com.mall.mission.model.entity.MissionRewardSettlement;
import com.mall.mission.model.entity.MissionTask;
import com.mall.mission.model.entity.MissionUserTask;
import com.mall.mission.model.vo.MissionInvestProjectVO;
import com.mall.mission.model.vo.MissionTaskStatsVO;
import com.mall.mission.model.vo.MissionTaskVO;
import com.mall.mission.service.MissionService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务中心状态机：配置来自数据库，用户记录承载领取/提交/审核，奖励只通过钱包 provider 入账。
 */
@Service
public class MissionServiceImpl implements MissionService {

    private static final int STATUS_ENABLED = 1;
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final int CURRENCY_SCALE = 6;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int MAX_RANK_LIMIT = 100;

    @Resource
    private MissionGoodsMapper goodsMapper;

    @Resource
    private MissionTaskMapper taskMapper;

    @Resource
    private MissionUserTaskMapper userTaskMapper;

    @Resource
    private MissionRewardSettlementMapper settlementMapper;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Page<MissionGoodsResp> goodsPage(MissionGoodsQueryReq req) throws BizException {
        MissionGoodsQueryReq q = req == null ? new MissionGoodsQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(MissionGoods.class)
                .eq(MissionGoods::getGoodsType, normalizeUpper(q.getGoodsType()), StringUtils.hasText(q.getGoodsType()))
                .eq(MissionGoods::getStatus, q.getStatus(), q.getStatus() != null)
                .and(w -> w.like(MissionGoods::getTitle, q.getKeyword())
                        .or(MissionGoods::getGoodsCode).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(MissionGoods::getSortOrder, true)
                .orderBy(MissionGoods::getId, false);
        Page<MissionGoods> page = goodsMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper);
        return page.map(this::toGoodsResp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MissionGoodsResp saveGoods(MissionGoodsReq req) throws BizException {
        Preconditions.notNull(req, MissionRespCodeEnum.GOODS_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getTitle()), MissionRespCodeEnum.GOODS_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getGoodsType()), MissionRespCodeEnum.GOODS_INVALID);
        MissionGoods item = req.getId() == null ? new MissionGoods() : goodsMapper.selectOneById(req.getId());
        Preconditions.notNull(item, MissionRespCodeEnum.GOODS_NOT_FOUND, req.getId());
        fillGoods(item, req);
        if (item.getId() == null) {
            goodsMapper.insert(item);
        } else {
            goodsMapper.update(item);
        }
        return toGoodsResp(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGoods(Long id) throws BizException {
        MissionGoods item = goodsMapper.selectOneById(id);
        Preconditions.notNull(item, MissionRespCodeEnum.GOODS_NOT_FOUND, id);
        goodsMapper.deleteById(id);
    }

    @Override
    public Page<MissionTaskResp> taskPage(MissionTaskQueryReq req) throws BizException {
        MissionTaskQueryReq q = req == null ? new MissionTaskQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(MissionTask.class)
                .eq(MissionTask::getTaskType, normalizeUpper(q.getTaskType()), StringUtils.hasText(q.getTaskType()))
                .eq(MissionTask::getStatus, q.getStatus(), q.getStatus() != null)
                .and(w -> w.like(MissionTask::getTitle, q.getKeyword())
                        .or(MissionTask::getTaskCode).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(MissionTask::getSortOrder, true)
                .orderBy(MissionTask::getId, false);
        Page<MissionTask> page = taskMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper);
        return page.map(this::toTaskResp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MissionTaskResp saveTask(MissionTaskReq req) throws BizException {
        Preconditions.notNull(req, MissionRespCodeEnum.TASK_INVALID);
        Preconditions.needTrue(StringUtils.hasText(req.getTitle()), MissionRespCodeEnum.TASK_INVALID);
        BigDecimal reward = normalizeAmount(req.getRewardAmount());
        MissionTask item = req.getId() == null ? new MissionTask() : taskMapper.selectOneById(req.getId());
        Preconditions.notNull(item, MissionRespCodeEnum.TASK_NOT_FOUND, req.getId());
        fillTask(item, req, reward);
        if (item.getId() == null) {
            taskMapper.insert(item);
        } else {
            taskMapper.update(item);
        }
        return toTaskResp(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTask(Long id) throws BizException {
        MissionTask item = taskMapper.selectOneById(id);
        Preconditions.notNull(item, MissionRespCodeEnum.TASK_NOT_FOUND, id);
        taskMapper.deleteById(id);
    }

    @Override
    public Page<MissionUserTaskResp> userTaskPage(MissionUserTaskQueryReq req) throws BizException {
        MissionUserTaskQueryReq q = req == null ? new MissionUserTaskQueryReq() : req;
        QueryWrapper wrapper = QueryWrapper.create()
                .from(MissionUserTask.class)
                .eq(MissionUserTask::getUserId, q.getUserId(), q.getUserId() != null)
                .eq(MissionUserTask::getTaskId, q.getTaskId(), q.getTaskId() != null)
                .eq(MissionUserTask::getTaskType, normalizeUpper(q.getTaskType()), StringUtils.hasText(q.getTaskType()))
                .eq(MissionUserTask::getStatus, normalizeUpper(q.getStatus()), StringUtils.hasText(q.getStatus()))
                .and(w -> w.like(MissionUserTask::getTaskTitle, q.getKeyword())
                        .or(MissionUserTask::getTaskCode).like(q.getKeyword()), StringUtils.hasText(q.getKeyword()))
                .orderBy(MissionUserTask::getId, false);
        Page<MissionUserTask> page = userTaskMapper.paginate(q.getPageNumber(), q.getPageSize(), wrapper);
        return page.map(this::toUserTaskResp);
    }

    @Override
    public List<LeaderboardItemResp> taskLeaderboard(Long startTime, Long endTime, Integer limit) throws BizException {
        AtomicInteger rank = new AtomicInteger(1);
        return userTaskMapper.taskLeaderboard(startTime, endTime, safeRankLimit(limit)).stream()
                .peek(item -> {
                    item.setRankNo(rank.getAndIncrement());
                    item.setType("TASK");
                    item.setMetricLabel("完成任务数");
                    item.setCurrency("COUNT");
                })
                .toList();
    }

    @Override
    public MissionUserTaskResp approve(MissionTaskReviewReq req) throws BizException {
        MissionUserTask locked = markApproved(req);
        MissionRewardSettlement settlement = settleReward(locked);
        return finishSettlement(locked.getId(), settlement);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MissionUserTaskResp reject(MissionTaskReviewReq req) throws BizException {
        Preconditions.notNull(req, MissionRespCodeEnum.REVIEW_REQUEST_INVALID);
        MissionUserTask record = userTaskMapper.selectOneById(req.getRecordId());
        Preconditions.notNull(record, MissionRespCodeEnum.USER_TASK_NOT_FOUND, req.getRecordId());
        Preconditions.needTrue(MissionUserTaskStatus.SUBMITTED.name().equals(record.getStatus()),
                MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        long now = System.currentTimeMillis();
        record.setStatus(MissionUserTaskStatus.REJECTED.name());
        record.setReviewRemark(req.getReviewRemark());
        record.setReviewedAt(now);
        record.setFinishedAt(now);
        userTaskMapper.update(record);
        return toUserTaskResp(record);
    }

    @Override
    public MissionTaskStatsVO userStats(Long userId, String taskType) throws BizException {
        Preconditions.notNull(userId, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        String normalizedTaskType = normalizeUpper(taskType);
        return MissionTaskStatsVO.builder()
                .completedCount(nz(userTaskMapper.countByStatus(userId, MissionUserTaskStatus.APPROVED.name(), normalizedTaskType)))
                .inProgressCount(nz(userTaskMapper.countInProgress(userId, normalizedTaskType)))
                .totalEarnings(nz(userTaskMapper.sumApprovedReward(userId, normalizedTaskType)))
                .build();
    }

    @Override
    public List<MissionTaskVO> userTasks(Long userId, String status, String taskType, Integer limit) throws BizException {
        Preconditions.notNull(userId, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        int safeLimit = safeLimit(limit);
        String normalizedTaskType = normalizeUpper(taskType);
        String s = StringUtils.hasText(status) ? status.trim().toLowerCase(Locale.ROOT) : "available";
        return switch (s) {
            case "available" -> userTaskMapper.listAvailable(userId, System.currentTimeMillis(), normalizedTaskType, safeLimit);
            case "in_progress" -> userTaskMapper.listInProgress(userId, normalizedTaskType, safeLimit);
            case "completed" -> userTaskMapper.listCompleted(userId, normalizedTaskType, safeLimit);
            default -> List.of();
        };
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MissionUserTaskResp claim(Long userId, Long taskId) throws BizException {
        Preconditions.notNull(userId, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        MissionTask task = taskMapper.selectOneById(taskId);
        Preconditions.notNull(task, MissionRespCodeEnum.TASK_NOT_FOUND, taskId);
        ensureTaskAvailable(task);

        MissionUserTask exists = findActiveUserTask(userId, taskId);
        Preconditions.needTrue(exists == null, MissionRespCodeEnum.USER_TASK_DUPLICATE);

        long now = System.currentTimeMillis();
        MissionUserTask record = new MissionUserTask();
        record.setUserId(userId);
        record.setTaskId(task.getId());
        record.setTaskCode(task.getTaskCode());
        record.setTaskTitle(task.getTitle());
        record.setTaskType(task.getTaskType());
        record.setCurrency(normalizeCurrency(task.getCurrency()));
        record.setRewardAmount(task.getRewardAmount());
        record.setStatus(MissionUserTaskStatus.CLAIMED.name());
        record.setClaimedAt(now);
        try {
            userTaskMapper.insert(record);
        } catch (DuplicateKeyException e) {
            MissionUserTask duplicate = findActiveUserTask(userId, taskId);
            Preconditions.notNull(duplicate, MissionRespCodeEnum.USER_TASK_DUPLICATE);
            return toUserTaskResp(duplicate);
        }
        return toUserTaskResp(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MissionUserTaskResp submit(Long userId, MissionSubmitDTO dto) throws BizException {
        Preconditions.notNull(userId, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        Preconditions.notNull(dto, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        MissionUserTask record = userTaskMapper.selectOneById(dto.getRecordId());
        Preconditions.notNull(record, MissionRespCodeEnum.USER_TASK_NOT_FOUND, dto.getRecordId());
        Preconditions.needTrue(userId.equals(record.getUserId()), MissionRespCodeEnum.USER_TASK_NOT_FOUND, dto.getRecordId());
        Preconditions.needTrue(MissionUserTaskStatus.CLAIMED.name().equals(record.getStatus())
                        || MissionUserTaskStatus.REJECTED.name().equals(record.getStatus()),
                MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        record.setStatus(MissionUserTaskStatus.SUBMITTED.name());
        record.setSubmitContent(trim(dto.getSubmitContent()));
        record.setSubmittedAt(System.currentTimeMillis());
        record.setReviewRemark(null);
        userTaskMapper.update(record);
        return toUserTaskResp(record);
    }

    @Override
    public List<MissionUserTaskResp> records(Long userId, String status, String taskType, Integer limit) throws BizException {
        Preconditions.notNull(userId, MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        QueryWrapper wrapper = QueryWrapper.create()
                .from(MissionUserTask.class)
                .eq(MissionUserTask::getUserId, userId)
                .eq(MissionUserTask::getTaskType, normalizeUpper(taskType), StringUtils.hasText(taskType))
                .eq(MissionUserTask::getStatus, normalizeUpper(status), StringUtils.hasText(status))
                .orderBy(MissionUserTask::getId, false)
                .limit(safeLimit(limit));
        return userTaskMapper.selectListByQuery(wrapper).stream()
                .map(this::toUserTaskResp)
                .toList();
    }

    @Override
    public List<MissionInvestProjectVO> investProjects(Integer limit) throws BizException {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(MissionGoods.class)
                .eq(MissionGoods::getGoodsType, MissionGoodsType.INVEST_PROJECT.name())
                .eq(MissionGoods::getStatus, STATUS_ENABLED)
                .orderBy(MissionGoods::getSortOrder, true)
                .orderBy(MissionGoods::getId, false)
                .limit(safeLimit(limit));
        return goodsMapper.selectListByQuery(wrapper).stream()
                .map(this::toInvestVo)
                .toList();
    }

    private MissionUserTask markApproved(MissionTaskReviewReq req) throws BizException {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return markApprovedTx(req);
                } catch (BizException e) {
                    throw new MissionTxRuntimeException(e);
                }
            });
        } catch (MissionTxRuntimeException e) {
            throw e.bizException;
        }
    }

    private MissionUserTask markApprovedTx(MissionTaskReviewReq req) throws BizException {
        Preconditions.notNull(req, MissionRespCodeEnum.REVIEW_REQUEST_INVALID);
        MissionUserTask record = userTaskMapper.selectOneById(req.getRecordId());
        Preconditions.notNull(record, MissionRespCodeEnum.USER_TASK_NOT_FOUND, req.getRecordId());
        if (MissionUserTaskStatus.APPROVED.name().equals(record.getStatus())) {
            return record;
        }
        Preconditions.needTrue(MissionUserTaskStatus.SUBMITTED.name().equals(record.getStatus()),
                MissionRespCodeEnum.USER_TASK_STATUS_INVALID);
        long now = System.currentTimeMillis();
        record.setStatus(MissionUserTaskStatus.APPROVED.name());
        record.setReviewRemark(req.getReviewRemark());
        record.setReviewedAt(now);
        userTaskMapper.update(record);
        ensureSettlement(record);
        return record;
    }

    private MissionRewardSettlement settleReward(MissionUserTask record) throws BizException {
        MissionRewardSettlement settlement = findSettlement(record.getId());
        Preconditions.notNull(settlement, MissionRespCodeEnum.SETTLEMENT_INVALID);
        if (MissionRewardSettlementStatus.SETTLED.name().equals(settlement.getStatus())) {
            return settlement;
        }
        WalletSettlementResp resp = applyWalletSettlement(settlement, record);
        settlement.setStatus(MissionRewardSettlementStatus.SETTLED.name());
        settlement.setWalletFlowNo(resp == null ? null : resp.getFlowNo());
        settlement.setFailReason(null);
        settlement.setSettledAt(System.currentTimeMillis());
        settlementMapper.update(settlement);
        return settlement;
    }

    private MissionUserTaskResp finishSettlement(Long userTaskId, MissionRewardSettlement settlement) throws BizException {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    MissionUserTask record = userTaskMapper.selectOneById(userTaskId);
                    Preconditions.notNull(record, MissionRespCodeEnum.USER_TASK_NOT_FOUND, userTaskId);
                    record.setWalletFlowNo(settlement.getWalletFlowNo());
                    record.setFinishedAt(settlement.getSettledAt());
                    userTaskMapper.update(record);
                    return toUserTaskResp(record);
                } catch (BizException e) {
                    throw new MissionTxRuntimeException(e);
                }
            });
        } catch (MissionTxRuntimeException e) {
            throw e.bizException;
        }
    }

    private void ensureSettlement(MissionUserTask record) {
        MissionRewardSettlement exists = findSettlement(record.getId());
        if (exists != null) {
            return;
        }
        MissionRewardSettlement settlement = new MissionRewardSettlement();
        settlement.setUserTaskId(record.getId());
        settlement.setUserId(record.getUserId());
        settlement.setTaskId(record.getTaskId());
        settlement.setCurrency(normalizeCurrency(record.getCurrency()));
        settlement.setAmount(record.getRewardAmount());
        settlement.setBizType(rewardBizType(record.getTaskType()));
        settlement.setBizId(String.valueOf(record.getId()));
        settlement.setStatus(MissionRewardSettlementStatus.PENDING.name());
        settlementMapper.insert(settlement);
    }

    private WalletSettlementResp applyWalletSettlement(MissionRewardSettlement settlement, MissionUserTask record) throws BizException {
        WalletSettlementReq req = new WalletSettlementReq();
        req.setUserId(settlement.getUserId());
        req.setCurrency(settlement.getCurrency());
        req.setBizType(settlement.getBizType());
        req.setBizId(settlement.getBizId());
        req.setAmount(settlement.getAmount());
        req.setRemark("任务奖励：" + record.getTaskTitle());
        return apiRestClient.post(serviceEndpoints.getWalletBaseUrl() + "/api/provider/wallet/settlement/apply",
                req, WalletSettlementResp.class);
    }

    private void ensureTaskAvailable(MissionTask task) throws BizException {
        long now = System.currentTimeMillis();
        Preconditions.needTrue(nz(task.getStatus()) == STATUS_ENABLED, MissionRespCodeEnum.TASK_NOT_AVAILABLE);
        Preconditions.needTrue(task.getStartAt() == null || task.getStartAt() <= now, MissionRespCodeEnum.TASK_NOT_AVAILABLE);
        Preconditions.needTrue(task.getEndAt() == null || task.getEndAt() >= now, MissionRespCodeEnum.TASK_NOT_AVAILABLE);
    }

    private MissionUserTask findActiveUserTask(Long userId, Long taskId) {
        return userTaskMapper.selectOneByQuery(QueryWrapper.create()
                .from(MissionUserTask.class)
                .eq(MissionUserTask::getUserId, userId)
                .eq(MissionUserTask::getTaskId, taskId)
                .in(MissionUserTask::getStatus, MissionUserTaskStatus.CLAIMED.name(),
                        MissionUserTaskStatus.SUBMITTED.name(), MissionUserTaskStatus.APPROVED.name(),
                        MissionUserTaskStatus.REJECTED.name())
                .limit(1));
    }

    private MissionRewardSettlement findSettlement(Long userTaskId) {
        return settlementMapper.selectOneByQuery(QueryWrapper.create()
                .from(MissionRewardSettlement.class)
                .eq(MissionRewardSettlement::getUserTaskId, userTaskId)
                .limit(1));
    }

    private void fillGoods(MissionGoods item, MissionGoodsReq req) throws BizException {
        item.setGoodsType(normalizeGoodsType(req.getGoodsType()));
        item.setGoodsCode(StringUtils.hasText(req.getGoodsCode()) ? req.getGoodsCode().trim() : defaultGoodsCode(req.getGoodsType()));
        item.setTitle(req.getTitle().trim());
        item.setDescription(trim(req.getDescription()));
        item.setCurrency(normalizeCurrency(req.getCurrency()));
        item.setMinAmount(nz(req.getMinAmount()));
        item.setMaxAmount(nz(req.getMaxAmount()));
        item.setDisplayRate(nz(req.getDisplayRate()));
        item.setCycleDays(req.getCycleDays() == null ? 0 : req.getCycleDays());
        item.setRiskLevel(StringUtils.hasText(req.getRiskLevel()) ? req.getRiskLevel().trim() : "LOW");
        item.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
    }

    private void fillTask(MissionTask item, MissionTaskReq req, BigDecimal reward) {
        item.setTaskCode(StringUtils.hasText(req.getTaskCode()) ? req.getTaskCode().trim() : defaultTaskCode());
        item.setTitle(req.getTitle().trim());
        item.setDescription(trim(req.getDescription()));
        item.setTaskType(StringUtils.hasText(req.getTaskType()) ? normalizeUpper(req.getTaskType()) : "GENERAL");
        item.setCurrency(normalizeCurrency(req.getCurrency()));
        item.setRewardAmount(reward);
        item.setRequiredVipLevel(req.getRequiredVipLevel() == null ? 0 : req.getRequiredVipLevel());
        item.setDailyLimit(req.getDailyLimit() == null ? 0 : req.getDailyLimit());
        item.setStartAt(req.getStartAt());
        item.setEndAt(req.getEndAt());
        item.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
    }

    private MissionGoodsResp toGoodsResp(MissionGoods item) {
        MissionGoodsResp resp = new MissionGoodsResp();
        resp.setId(item.getId());
        resp.setGoodsType(item.getGoodsType());
        resp.setGoodsCode(item.getGoodsCode());
        resp.setTitle(item.getTitle());
        resp.setDescription(item.getDescription());
        resp.setCurrency(item.getCurrency());
        resp.setMinAmount(item.getMinAmount());
        resp.setMaxAmount(item.getMaxAmount());
        resp.setDisplayRate(item.getDisplayRate());
        resp.setCycleDays(item.getCycleDays());
        resp.setRiskLevel(item.getRiskLevel());
        resp.setSortOrder(item.getSortOrder());
        resp.setStatus(item.getStatus());
        resp.setCreateTime(item.getCreateTime());
        resp.setUpdateTime(item.getUpdateTime());
        return resp;
    }

    private MissionTaskResp toTaskResp(MissionTask item) {
        MissionTaskResp resp = new MissionTaskResp();
        resp.setId(item.getId());
        resp.setTaskCode(item.getTaskCode());
        resp.setTitle(item.getTitle());
        resp.setDescription(item.getDescription());
        resp.setTaskType(item.getTaskType());
        resp.setCurrency(item.getCurrency());
        resp.setRewardAmount(item.getRewardAmount());
        resp.setRequiredVipLevel(item.getRequiredVipLevel());
        resp.setDailyLimit(item.getDailyLimit());
        resp.setStartAt(item.getStartAt());
        resp.setEndAt(item.getEndAt());
        resp.setSortOrder(item.getSortOrder());
        resp.setStatus(item.getStatus());
        resp.setCreateTime(item.getCreateTime());
        resp.setUpdateTime(item.getUpdateTime());
        return resp;
    }

    private MissionUserTaskResp toUserTaskResp(MissionUserTask item) {
        MissionUserTaskResp resp = new MissionUserTaskResp();
        resp.setId(item.getId());
        resp.setUserId(item.getUserId());
        resp.setTaskId(item.getTaskId());
        resp.setTaskCode(item.getTaskCode());
        resp.setTaskTitle(item.getTaskTitle());
        resp.setTaskType(item.getTaskType());
        resp.setCurrency(item.getCurrency());
        resp.setRewardAmount(item.getRewardAmount());
        resp.setStatus(item.getStatus());
        resp.setSubmitContent(item.getSubmitContent());
        resp.setReviewRemark(item.getReviewRemark());
        resp.setWalletFlowNo(item.getWalletFlowNo());
        resp.setClaimedAt(item.getClaimedAt());
        resp.setSubmittedAt(item.getSubmittedAt());
        resp.setReviewedAt(item.getReviewedAt());
        resp.setFinishedAt(item.getFinishedAt());
        resp.setCreateTime(item.getCreateTime());
        resp.setUpdateTime(item.getUpdateTime());
        return resp;
    }

    private MissionInvestProjectVO toInvestVo(MissionGoods item) {
        MissionInvestProjectVO vo = new MissionInvestProjectVO();
        vo.setId(item.getId());
        vo.setGoodsCode(item.getGoodsCode());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setCurrency(item.getCurrency());
        vo.setMinAmount(item.getMinAmount());
        vo.setMaxAmount(item.getMaxAmount());
        vo.setDisplayRate(item.getDisplayRate());
        vo.setCycleDays(item.getCycleDays());
        vo.setRiskLevel(item.getRiskLevel());
        return vo;
    }

    private String normalizeGoodsType(String value) throws BizException {
        String normalized = normalizeUpper(value);
        try {
            return MissionGoodsType.valueOf(normalized).name();
        } catch (IllegalArgumentException e) {
            throw new BizException(MissionRespCodeEnum.GOODS_INVALID, null);
        }
    }

    private String rewardBizType(String taskType) {
        String normalized = normalizeUpper(taskType);
        return switch (normalized == null ? "" : normalized) {
            case "TASK_CENTER" -> "TASK_CENTER_REWARD";
            case "SHARE" -> "SHARE_TASK_REWARD";
            case "VIDEO" -> "VIDEO_TASK_REWARD";
            case "VA" -> "VA_TASK_REWARD";
            default -> "TASK_REWARD";
        };
    }

    private String normalizeCurrency(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY;
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) throws BizException {
        Preconditions.needTrue(amount != null && amount.signum() > 0, MissionRespCodeEnum.TASK_INVALID);
        Preconditions.needTrue(amount.scale() <= CURRENCY_SCALE, MissionRespCodeEnum.TASK_INVALID);
        return amount;
    }

    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int safeRankLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_RANK_LIMIT);
    }

    private Long nz(Long value) {
        return value == null ? 0L : value;
    }

    private Integer nz(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultGoodsCode(String goodsType) {
        return normalizeUpper(goodsType) + "-" + System.currentTimeMillis();
    }

    private String defaultTaskCode() {
        return "TASK-" + System.currentTimeMillis();
    }

    private static class MissionTxRuntimeException extends RuntimeException {
        private final BizException bizException;

        MissionTxRuntimeException(BizException bizException) {
            this.bizException = bizException;
        }
    }
}