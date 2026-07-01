package com.mall.user.service.impl;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.VipLevelConfigReq;
import com.mall.common.model.dto.req.WalletSettlementReq;
import com.mall.common.model.dto.resp.VipLevelConfigResp;
import com.mall.common.model.dto.resp.WalletSettlementResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.user.configuration.properties.ServiceEndpointProperties;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.mapper.VipLevelConfigMapper;
import com.mall.user.mapper.VipUpgradeOrderMapper;
import com.mall.user.model.dto.VipLevelUpDTO;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.entity.VipLevelConfig;
import com.mall.user.model.entity.VipUpgradeOrder;
import com.mall.user.model.vo.VipLevelConfigVO;
import com.mall.user.model.vo.VipLevelOverviewVO;
import com.mall.user.model.vo.VipUpgradeOrderVO;
import com.mall.user.service.InviteCommissionService;
import com.mall.user.service.VipService;
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
import java.util.UUID;

/**
 * VIP 状态机：配置可运维，升级必须落订单并通过钱包 provider 扣款。
 * 资金方向由钱包端 VIP_UPGRADE 固定为 OUT，用户服务只传业务事实与幂等键。
 */
@Service
public class VipServiceImpl implements VipService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;
    private static final String DEFAULT_CURRENCY = "USDT";
    private static final int CURRENCY_SCALE = 6;
    private static final String ORDER_PENDING = "PENDING";
    private static final String ORDER_SUCCESS = "SUCCESS";

    @Resource
    private VipLevelConfigMapper vipLevelConfigMapper;

    @Resource
    private VipUpgradeOrderMapper vipUpgradeOrderMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private InviteCommissionService inviteCommissionService;

    @Override
    public VipLevelOverviewVO userOverview(long userId) throws BizException {
        UserInfo user = findUser(userId);
        List<VipLevelConfigVO> levels = listEnabledConfigs().stream()
                .map(this::toUserVo)
                .toList();
        return VipLevelOverviewVO.builder()
                .currentLevel(nz(user.getVipLevel()))
                .levels(levels)
                .build();
    }

    @Override
    public VipUpgradeOrderVO upgrade(long userId, VipLevelUpDTO dto) throws BizException {
        UpgradeDraft draft = createPendingOrder(userId, dto);
        if (ORDER_SUCCESS.equals(draft.order().getStatus())) {
            inviteCommissionService.settleVipUpgrade(draft.order().getOrderNo());
            return toOrderVo(draft.order());
        }
        WalletSettlementResp settlement = settle(draft.order());
        VipUpgradeOrderVO result = finishUpgrade(draft.order().getOrderNo(), settlement);
        inviteCommissionService.settleVipUpgrade(result.orderNo());
        return result;
    }

    private UpgradeDraft createPendingOrder(long userId, VipLevelUpDTO dto) throws BizException {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return createPendingOrderTx(userId, dto);
                } catch (BizException e) {
                    throw new VipTxRuntimeException(e);
                }
            });
        } catch (VipTxRuntimeException e) {
            throw e.bizException;
        }
    }

    private UpgradeDraft createPendingOrderTx(long userId, VipLevelUpDTO dto) throws BizException {
        Preconditions.notNull(dto, UserRespCodeEnum.VIP_LEVEL_INVALID);
        Integer targetLevel = dto.level();
        Preconditions.notNull(targetLevel, UserRespCodeEnum.VIP_LEVEL_INVALID);
        VipUpgradeOrder success = findSuccessOrder(userId, targetLevel);
        if (success != null) {
            return new UpgradeDraft(success);
        }

        UserInfo user = findUser(userId);
        int currentLevel = nz(user.getVipLevel());
        Preconditions.needTrue(targetLevel > currentLevel, UserRespCodeEnum.VIP_LEVEL_INVALID);

        VipUpgradeOrder pending = findPendingOrder(userId, targetLevel);
        if (pending != null) {
            return new UpgradeDraft(pending);
        }

        VipLevelConfig config = findEnabledConfig(targetLevel);
        Preconditions.notNull(config, UserRespCodeEnum.VIP_CONFIG_NOT_FOUND, targetLevel);
        BigDecimal amount = normalizePrice(config.getPrice());
        Preconditions.needTrue(amount.signum() > 0, UserRespCodeEnum.VIP_CONFIG_INVALID);
        Preconditions.needTrue(amount.scale() <= CURRENCY_SCALE, UserRespCodeEnum.VIP_CONFIG_INVALID);

        VipUpgradeOrder order = new VipUpgradeOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(userId);
        order.setFromLevel(currentLevel);
        order.setToLevel(targetLevel);
        order.setCurrency(DEFAULT_CURRENCY);
        order.setAmount(amount);
        order.setStatus(ORDER_PENDING);
        order.setRemark("VIP升级：VIP" + currentLevel + " -> VIP" + targetLevel);
        try {
            vipUpgradeOrderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            VipUpgradeOrder exists = findPendingOrder(userId, targetLevel);
            if (exists == null) {
                exists = findSuccessOrder(userId, targetLevel);
            }
            Preconditions.notNull(exists, UserRespCodeEnum.VIP_CONFIG_INVALID);
            return new UpgradeDraft(exists);
        }
        return new UpgradeDraft(order);
    }

    private VipUpgradeOrderVO finishUpgrade(String orderNo, WalletSettlementResp settlement) throws BizException {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return finishUpgradeTx(orderNo, settlement);
                } catch (BizException e) {
                    throw new VipTxRuntimeException(e);
                }
            });
        } catch (VipTxRuntimeException e) {
            throw e.bizException;
        }
    }

    private VipUpgradeOrderVO finishUpgradeTx(String orderNo, WalletSettlementResp settlement) throws BizException {
        VipUpgradeOrder order = findOrder(orderNo);
        Preconditions.notNull(order, UserRespCodeEnum.VIP_CONFIG_INVALID);
        if (ORDER_SUCCESS.equals(order.getStatus())) {
            return toOrderVo(order);
        }

        UserInfo user = findUser(order.getUserId());
        if (nz(user.getVipLevel()) < order.getToLevel()) {
            UserInfo update = new UserInfo();
            update.setId(user.getId());
            update.setVipLevel(order.getToLevel());
            userInfoMapper.update(update);
        }

        order.setStatus(ORDER_SUCCESS);
        order.setWalletFlowNo(settlement == null ? null : settlement.getFlowNo());
        order.setFinishedAt(System.currentTimeMillis());
        vipUpgradeOrderMapper.update(order);
        return toOrderVo(order);
    }

    @Override
    public List<VipLevelConfigResp> configList(Integer status) throws BizException {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(VipLevelConfig.class)
                .eq(VipLevelConfig::getStatus, status, status != null)
                .orderBy(VipLevelConfig::getSortOrder, true)
                .orderBy(VipLevelConfig::getLevel, true);
        return vipLevelConfigMapper.selectListByQuery(wrapper).stream()
                .map(this::toResp)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public VipLevelConfigResp saveConfig(VipLevelConfigReq req) throws BizException {
        Preconditions.notNull(req, UserRespCodeEnum.VIP_CONFIG_INVALID);
        Preconditions.needTrue(req.getLevel() != null && req.getLevel() >= 0, UserRespCodeEnum.VIP_LEVEL_INVALID);
        Preconditions.needTrue(req.getPrice() != null && req.getPrice().signum() >= 0, UserRespCodeEnum.VIP_CONFIG_INVALID);
        Preconditions.needTrue(req.getLevel() == 0 || req.getPrice().signum() > 0, UserRespCodeEnum.VIP_CONFIG_INVALID);
        Preconditions.needTrue(req.getPrice().scale() <= CURRENCY_SCALE, UserRespCodeEnum.VIP_CONFIG_INVALID);
        VipLevelConfig exists = findByLevel(req.getLevel());
        Preconditions.needTrue(exists == null || req.getId() != null && exists.getId().equals(req.getId()),
                UserRespCodeEnum.VIP_LEVEL_REPEAT, req.getLevel());

        VipLevelConfig item = req.getId() == null ? new VipLevelConfig() : vipLevelConfigMapper.selectOneById(req.getId());
        Preconditions.notNull(item, UserRespCodeEnum.VIP_CONFIG_NOT_FOUND, req.getId());
        fill(item, req);
        if (item.getId() == null) {
            vipLevelConfigMapper.insert(item);
        } else {
            vipLevelConfigMapper.update(item);
        }
        return toResp(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteConfig(Long id) throws BizException {
        VipLevelConfig item = vipLevelConfigMapper.selectOneById(id);
        Preconditions.notNull(item, UserRespCodeEnum.VIP_CONFIG_NOT_FOUND, id);
        Preconditions.needTrue(item.getLevel() == null || item.getLevel() > 0, UserRespCodeEnum.VIP_BASE_LEVEL_LOCKED);
        vipLevelConfigMapper.deleteById(id);
    }

    private WalletSettlementResp settle(VipUpgradeOrder order) throws BizException {
        WalletSettlementReq req = new WalletSettlementReq();
        req.setUserId(order.getUserId());
        req.setCurrency(order.getCurrency());
        req.setBizType("VIP_UPGRADE");
        req.setBizId(order.getOrderNo());
        req.setAmount(order.getAmount());
        req.setRemark(StringUtils.hasText(order.getRemark()) ? order.getRemark() : "VIP升级扣款");
        return apiRestClient.post(serviceEndpoints.getWalletBaseUrl() + "/api/provider/wallet/settlement/apply",
                req, WalletSettlementResp.class);
    }

    private UserInfo findUser(long userId) throws BizException {
        UserInfo user = userInfoMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .eq(UserInfo::getUserId, userId));
        Preconditions.notNull(user, UserRespCodeEnum.USER_NOT_EXIST);
        return user;
    }

    private VipUpgradeOrder findOrder(String orderNo) {
        return vipUpgradeOrderMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipUpgradeOrder.class)
                .eq(VipUpgradeOrder::getOrderNo, orderNo)
                .limit(1));
    }

    private VipUpgradeOrder findPendingOrder(Long userId, Integer toLevel) {
        return vipUpgradeOrderMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipUpgradeOrder.class)
                .eq(VipUpgradeOrder::getUserId, userId)
                .eq(VipUpgradeOrder::getToLevel, toLevel)
                .eq(VipUpgradeOrder::getStatus, ORDER_PENDING)
                .limit(1));
    }

    private VipUpgradeOrder findSuccessOrder(Long userId, Integer toLevel) {
        return vipUpgradeOrderMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipUpgradeOrder.class)
                .eq(VipUpgradeOrder::getUserId, userId)
                .eq(VipUpgradeOrder::getToLevel, toLevel)
                .eq(VipUpgradeOrder::getStatus, ORDER_SUCCESS)
                .limit(1));
    }

    private List<VipLevelConfig> listEnabledConfigs() {
        return vipLevelConfigMapper.selectListByQuery(QueryWrapper.create()
                .from(VipLevelConfig.class)
                .eq(VipLevelConfig::getStatus, STATUS_ENABLED)
                .orderBy(VipLevelConfig::getSortOrder, true)
                .orderBy(VipLevelConfig::getLevel, true));
    }

    private VipLevelConfig findEnabledConfig(Integer level) {
        return vipLevelConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipLevelConfig.class)
                .eq(VipLevelConfig::getLevel, level)
                .eq(VipLevelConfig::getStatus, STATUS_ENABLED)
                .limit(1));
    }

    private VipLevelConfig findByLevel(Integer level) {
        return vipLevelConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(VipLevelConfig.class)
                .eq(VipLevelConfig::getLevel, level)
                .limit(1));
    }

    private void fill(VipLevelConfig item, VipLevelConfigReq req) {
        item.setLevel(req.getLevel());
        item.setLevelName(trim(req.getLevelName(), "VIP" + req.getLevel()));
        item.setPrice(normalizePrice(req.getPrice()));
        item.setRebateRate(req.getRebateRate() == null ? BigDecimal.ZERO : req.getRebateRate());
        item.setDailyTasks(req.getDailyTasks() == null ? 0 : req.getDailyTasks());
        item.setBenefits(trim(req.getBenefits(), ""));
        item.setSortOrder(req.getSortOrder() == null ? req.getLevel() : req.getSortOrder());
        item.setStatus(req.getStatus() == null ? STATUS_ENABLED : req.getStatus());
        if (item.getStatus() != STATUS_ENABLED) {
            item.setStatus(STATUS_DISABLED);
        }
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trim(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private VipLevelConfigVO toUserVo(VipLevelConfig c) {
        return VipLevelConfigVO.builder()
                .level(c.getLevel())
                .levelName(c.getLevelName())
                .price(c.getPrice())
                .rebateRate(c.getRebateRate())
                .dailyTasks(c.getDailyTasks())
                .benefits(c.getBenefits())
                .build();
    }

    private VipLevelConfigResp toResp(VipLevelConfig c) {
        VipLevelConfigResp resp = new VipLevelConfigResp();
        resp.setId(c.getId());
        resp.setLevel(c.getLevel());
        resp.setLevelName(c.getLevelName());
        resp.setPrice(c.getPrice());
        resp.setRebateRate(c.getRebateRate());
        resp.setDailyTasks(c.getDailyTasks());
        resp.setBenefits(c.getBenefits());
        resp.setSortOrder(c.getSortOrder());
        resp.setStatus(c.getStatus());
        resp.setCreateTime(c.getCreateTime());
        resp.setUpdateTime(c.getUpdateTime());
        return resp;
    }

    private VipUpgradeOrderVO toOrderVo(VipUpgradeOrder o) {
        return VipUpgradeOrderVO.builder()
                .orderNo(o.getOrderNo())
                .fromLevel(o.getFromLevel())
                .toLevel(o.getToLevel())
                .currency(o.getCurrency())
                .amount(o.getAmount())
                .status(o.getStatus())
                .walletFlowNo(o.getWalletFlowNo())
                .finishedAt(o.getFinishedAt())
                .build();
    }

    private String nextOrderNo() {
        return "V" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private record UpgradeDraft(VipUpgradeOrder order) {
    }

    private static class VipTxRuntimeException extends RuntimeException {

        private final BizException bizException;

        private VipTxRuntimeException(BizException bizException) {
            super(bizException);
            this.bizException = bizException;
        }
    }
}