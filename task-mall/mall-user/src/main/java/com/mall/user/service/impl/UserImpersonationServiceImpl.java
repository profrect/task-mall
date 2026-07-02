package com.mall.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.mall.common.auth.util.AuthUtils;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import com.mall.common.core.util.Preconditions;
import com.mall.common.model.dto.req.UserImpersonationExchangeReq;
import com.mall.common.model.dto.req.UserImpersonationTicketReq;
import com.mall.common.model.dto.resp.UserImpersonationTicketResp;
import com.mall.user.enums.UserImpersonationTicketStatus;
import com.mall.user.enums.UserRespCodeEnum;
import com.mall.user.mapper.UserImpersonationTicketMapper;
import com.mall.user.mapper.UserInfoMapper;
import com.mall.user.model.entity.UserImpersonationTicket;
import com.mall.user.model.entity.UserInfo;
import com.mall.user.model.vo.UserLoginVO;
import com.mall.user.service.UserImpersonationService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 后台代登录前台采用一次性票据：后台永不拿用户 token，H5 自行兑换并进入带审计标记的用户会话。
 */
@Service
public class UserImpersonationServiceImpl implements UserImpersonationService {

    private static final int STATUS_NORMAL = 1;
    private static final int TICKET_EXPIRES_SECONDS = 300;
    private static final int MAX_TICKET_RETRY = 3;
    private static final int RANDOM_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    private UserImpersonationTicketMapper ticketMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public UserImpersonationTicketResp createTicket(UserImpersonationTicketReq req) throws BizException {
        Preconditions.notNull(req, CommonRespCode.PARAM_MISSING);
        Preconditions.notNull(req.getUserId(), UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(StringUtils.hasText(req.getAdminAccount()), UserRespCodeEnum.IMPERSONATION_ADMIN_INVALID);

        UserInfo target = userInfoMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .eq(UserInfo::getUserId, req.getUserId())
                .limit(1));
        Preconditions.notNull(target, UserRespCodeEnum.USER_NOT_EXIST);
        Preconditions.needTrue(target.getStatus() == null || target.getStatus() == STATUS_NORMAL,
                UserRespCodeEnum.ACCOUNT_FROZEN);

        long now = System.currentTimeMillis();
        long expiresAt = now + TICKET_EXPIRES_SECONDS * 1000L;
        for (int i = 0; i < MAX_TICKET_RETRY; i++) {
            String ticket = randomTicket();
            UserImpersonationTicket record = new UserImpersonationTicket();
            record.setTicketNo("IMP" + IdUtil.getSnowflakeNextIdStr());
            record.setTicketHash(sha256Hex(ticket));
            record.setTargetUserId(req.getUserId());
            record.setAdminAccount(trimTo(req.getAdminAccount(), 64));
            record.setAdminIp(trimTo(req.getAdminIp(), 64));
            record.setUserAgent(trimTo(req.getUserAgent(), 512));
            record.setStatus(UserImpersonationTicketStatus.CREATED.name());
            record.setExpiresAt(expiresAt);
            try {
                ticketMapper.insert(record);
                UserImpersonationTicketResp resp = new UserImpersonationTicketResp();
                resp.setTicket(ticket);
                resp.setExpiresIn(TICKET_EXPIRES_SECONDS);
                resp.setExpiresAt(expiresAt);
                return resp;
            } catch (DuplicateKeyException ex) {
                // 极小概率随机碰撞，重新生成票据即可。
            }
        }
        throw new BizException(CommonRespCode.SYSTEM_BUSY, null);
    }

    @Override
    public UserLoginVO exchange(UserImpersonationExchangeReq req) throws BizException {
        Preconditions.notNull(req, CommonRespCode.PARAM_MISSING);
        Preconditions.needTrue(StringUtils.hasText(req.getTicket()), UserRespCodeEnum.IMPERSONATION_TICKET_INVALID);

        UserImpersonationTicket record = ticketMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserImpersonationTicket.class)
                .eq(UserImpersonationTicket::getTicketHash, sha256Hex(req.getTicket().trim()))
                .limit(1));
        Preconditions.notNull(record, UserRespCodeEnum.IMPERSONATION_TICKET_INVALID);
        Preconditions.needTrue(UserImpersonationTicketStatus.CREATED.name().equals(record.getStatus()),
                UserRespCodeEnum.IMPERSONATION_TICKET_INVALID);

        long now = System.currentTimeMillis();
        if (record.getExpiresAt() == null || record.getExpiresAt() < now) {
            expire(record, "ticket expired");
            throw new BizException(UserRespCodeEnum.IMPERSONATION_TICKET_EXPIRED, null);
        }

        UserInfo target = userInfoMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserInfo.class)
                .eq(UserInfo::getUserId, record.getTargetUserId())
                .limit(1));
        Preconditions.notNull(target, UserRespCodeEnum.USER_NOT_EXIST);
        if (target.getStatus() != null && target.getStatus() != STATUS_NORMAL) {
            expire(record, "target user frozen");
            throw new BizException(UserRespCodeEnum.ACCOUNT_FROZEN, null);
        }

        UserImpersonationTicket patch = new UserImpersonationTicket();
        patch.setStatus(UserImpersonationTicketStatus.CONSUMED.name());
        patch.setConsumedAt(now);
        int updated = ticketMapper.updateByQuery(patch, QueryWrapper.create()
                .from(UserImpersonationTicket.class)
                .eq(UserImpersonationTicket::getId, record.getId())
                .eq(UserImpersonationTicket::getStatus, UserImpersonationTicketStatus.CREATED.name()));
        Preconditions.needTrue(updated > 0, UserRespCodeEnum.IMPERSONATION_TICKET_INVALID);

        StpUtil.login(record.getTargetUserId());
        AuthUtils.markImpersonation(record.getAdminAccount(), record.getTargetUserId(), record.getId());
        return UserLoginVO.builder()
                .accessToken(StpUtil.getTokenValue())
                .expiresIn(StpUtil.getTokenTimeout())
                .tokenType("Bearer")
                .build();
    }

    private void expire(UserImpersonationTicket record, String reason) {
        UserImpersonationTicket patch = new UserImpersonationTicket();
        patch.setStatus(UserImpersonationTicketStatus.EXPIRED.name());
        patch.setFailReason(trimTo(reason, 500));
        ticketMapper.updateByQuery(patch, QueryWrapper.create()
                .from(UserImpersonationTicket.class)
                .eq(UserImpersonationTicket::getId, record.getId())
                .eq(UserImpersonationTicket::getStatus, UserImpersonationTicketStatus.CREATED.name()));
    }

    private String randomTicket() {
        byte[] bytes = new byte[RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String trimTo(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}