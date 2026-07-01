package com.mall.wallet.transfer;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.UserExistReq;
import com.mall.common.model.dto.resp.UserExistResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mall.wallet.configuration.properties.ServiceEndpointProperties;
import com.mall.wallet.enums.WalletBizType;
import com.mall.wallet.enums.WalletRespCodeEnum;
import com.mall.wallet.model.LedgerEvent;
import com.mall.wallet.service.WalletAccountService;
import com.mall.wallet.transfer.mapper.WalletTransferOrderMapper;
import com.mall.wallet.transfer.model.entity.WalletTransferOrder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 站内转账编排：校验收款会员存在后，同事务写订单、双方账变。 */
@Service
public class TransferService {

    private static final String DEFAULT_COIN = "USDT";
    private static final int USER_STATUS_NORMAL = 1;
    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;

    @Resource
    private WalletTransferOrderMapper transferOrderMapper;

    @Resource
    private WalletAccountService walletAccountService;

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Transactional(rollbackFor = Exception.class)
    public WalletTransferOrder apply(Long fromUserId, Long toUserId, String coin, BigDecimal amount, String remark)
            throws BizException {
        Preconditions.notNull(fromUserId, WalletRespCodeEnum.TRANSFER_USER_INVALID);
        Preconditions.notNull(toUserId, WalletRespCodeEnum.TRANSFER_USER_INVALID);
        Preconditions.needTrue(!fromUserId.equals(toUserId), WalletRespCodeEnum.TRANSFER_SELF_NOT_ALLOWED);
        Preconditions.needTrue(amount != null && amount.signum() > 0, WalletRespCodeEnum.INVALID_AMOUNT, 6);
        ensureReceiverValid(toUserId);

        String coinSymbol = StringUtils.hasText(coin) ? coin.trim().toUpperCase(Locale.ROOT) : DEFAULT_COIN;
        String orderNo = nextOrderNo();

        walletAccountService.getOrCreate(fromUserId, coinSymbol);
        walletAccountService.getOrCreate(toUserId, coinSymbol);

        WalletTransferOrder order = new WalletTransferOrder();
        order.setOrderNo(orderNo);
        order.setFromUserId(fromUserId);
        order.setToUserId(toUserId);
        order.setCoin(coinSymbol);
        order.setAmount(amount);
        order.setStatus(TransferStatus.SUCCESS.name());
        order.setRemark(remark);
        order.setFinishedAt(System.currentTimeMillis());
        transferOrderMapper.insert(order);

        walletAccountService.applyLedger(new LedgerEvent(
                fromUserId, coinSymbol, WalletBizType.TRANSFER_OUT, orderNo, amount,
                transferRemark("转出", toUserId, remark)));
        walletAccountService.applyLedger(new LedgerEvent(
                toUserId, coinSymbol, WalletBizType.TRANSFER_IN, orderNo, amount,
                transferRemark("转入", fromUserId, remark)));
        return order;
    }

    public List<WalletTransferOrder> listForUser(Long userId) {
        return transferOrderMapper.listByUser(userId);
    }

    public List<WalletTransferOrder> listForAdmin(Long userId, Integer limit) {
        int safe = safeLimit(limit);
        if (userId != null && userId > 0) {
            return transferOrderMapper.listForAdminByUser(userId, safe);
        }
        return transferOrderMapper.listForAdmin(safe);
    }

    private void ensureReceiverValid(Long userId) throws BizException {
        UserExistReq req = new UserExistReq();
        req.setUserId(userId);
        UserExistResp resp = apiRestClient.post(serviceEndpoints.getUserBaseUrl() + "/api/provider/user/exists",
                req, UserExistResp.class);
        Preconditions.needTrue(resp != null && Boolean.TRUE.equals(resp.getExists()),
                WalletRespCodeEnum.TRANSFER_RECEIVER_NOT_FOUND, userId);
        Preconditions.needTrue(resp.getStatus() == null || resp.getStatus() == USER_STATUS_NORMAL,
                WalletRespCodeEnum.TRANSFER_RECEIVER_NOT_AVAILABLE, userId);
    }

    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String transferRemark(String action, Long peerUserId, String remark) {
        String base = "站内转账" + action + "：" + peerUserId;
        return StringUtils.hasText(remark) ? base + "，" + remark.trim() : base;
    }

    private String nextOrderNo() {
        return "T" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}