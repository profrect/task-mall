package com.mall.admin.service.user.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.admin.configuration.properties.ServiceEndpointProperties;
import com.mall.admin.model.dto.UserInfoDTO;
import com.mall.admin.model.vo.UserInfoVO;
import com.mall.admin.service.user.UserService;
import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.req.UserReq;
import com.mall.common.model.dto.req.UserStatusUpdateReq;
import com.mall.common.model.dto.req.WalletAccountBatchReq;
import com.mall.common.model.dto.resp.UserResp;
import com.mall.common.model.dto.resp.WalletAccountResp;
import com.mall.common.web.rest.ApiRestClient;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 会员管理聚合层：会员基础资料来自 mall-user，余额快照来自 mall-wallet。
 * <p>
 * admin 不持有会员状态、不直接读其它服务数据库，只做跨服务数据装配；未开通钱包的会员按 0 余额展示。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String USER_PAGE_PATH = "/api/provider/user/user-page";
    private static final String USER_STATUS_PATH = "/api/provider/user/status";
    private static final String WALLET_BATCH_PATH = "/api/provider/wallet/account/batch";
    private static final String DEFAULT_CURRENCY = "USDT";

    @Resource
    private ApiRestClient apiRestClient;

    @Resource
    private ServiceEndpointProperties serviceEndpoints;

    @Override
    public Page<UserInfoVO> userPage(UserInfoDTO dto) throws BizException {
        UserReq userReq = new UserReq();
        userReq.setPageNumber(dto.getPageNumber());
        userReq.setPageSize(dto.getPageSize());

        Page<UserResp> userPage = apiRestClient.post(userUrl(USER_PAGE_PATH), userReq,
                new TypeReference<Page<UserResp>>() {});
        List<UserResp> users = userPage == null || userPage.getRecords() == null ? List.of() : userPage.getRecords();
        if (users.isEmpty()) {
            return new Page<>(List.of(), pageNumber(userPage, dto), pageSize(userPage, dto), totalRow(userPage));
        }

        Map<Long, WalletAccountResp> walletMap = loadWalletMap(users);
        List<UserInfoVO> rows = users.stream()
                .map(user -> toVo(user, walletMap.get(user.getUserId())))
                .toList();
        return new Page<>(rows, userPage.getPageNumber(), userPage.getPageSize(), userPage.getTotalRow());
    }

    @Override
    public void updateStatus(Long userId, Integer status) throws BizException {
        UserStatusUpdateReq req = new UserStatusUpdateReq();
        req.setUserId(userId);
        req.setStatus(status);
        apiRestClient.post(userUrl(USER_STATUS_PATH), req, Void.class);
    }

    private Map<Long, WalletAccountResp> loadWalletMap(List<UserResp> users) throws BizException {
        List<Long> userIds = users.stream()
                .map(UserResp::getUserId)
                .filter(id -> id != null && id > 0)
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        WalletAccountBatchReq req = new WalletAccountBatchReq();
        req.setUserIds(userIds);
        req.setCurrency(DEFAULT_CURRENCY);
        List<WalletAccountResp> accounts = apiRestClient.post(walletUrl(WALLET_BATCH_PATH), req,
                new TypeReference<List<WalletAccountResp>>() {});
        if (accounts == null || accounts.isEmpty()) {
            return Map.of();
        }
        return accounts.stream().collect(Collectors.toMap(WalletAccountResp::getUserId, Function.identity(), (a, b) -> a));
    }

    private UserInfoVO toVo(UserResp user, WalletAccountResp wallet) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setVipLevel(user.getVipLevel());
        vo.setInviteCode(user.getInviteCode());
        vo.setStatus(user.getStatus());
        vo.setParentUserName(user.getParentUserName());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setAvailableAmt(wallet == null ? BigDecimal.ZERO : wallet.getAvailBalance());
        vo.setFreezeAmt(wallet == null ? BigDecimal.ZERO : wallet.getFrozenBalance());
        return vo;
    }

    private long pageNumber(Page<UserResp> page, UserInfoDTO dto) {
        return page == null ? dto.getPageNumber() : page.getPageNumber();
    }

    private long pageSize(Page<UserResp> page, UserInfoDTO dto) {
        return page == null ? dto.getPageSize() : page.getPageSize();
    }

    private long totalRow(Page<UserResp> page) {
        return page == null ? 0 : page.getTotalRow();
    }

    private String userUrl(String path) {
        return serviceEndpoints.getUserBaseUrl() + path;
    }

    private String walletUrl(String path) {
        return serviceEndpoints.getWalletBaseUrl() + path;
    }
}