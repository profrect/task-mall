package com.mall.admin.service.audit.impl;

import com.mall.admin.mapper.AdminOperationLogMapper;
import com.mall.admin.model.dto.OperationLogQueryDTO;
import com.mall.admin.model.entity.AdminOperationLog;
import com.mall.admin.model.vo.OperationLogVO;
import com.mall.admin.service.audit.OperationLogService;
import com.mall.common.core.context.UserContext;
import com.mall.common.core.exception.BizException;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 后台操作日志服务实现。 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 500;

    @Resource
    private AdminOperationLogMapper operationLogMapper;

    @Override
    public List<OperationLogVO> list(OperationLogQueryDTO query) throws BizException {
        int limit = normalizeLimit(query.getLimit());
        QueryWrapper wrapper = QueryWrapper.create()
                .from(AdminOperationLog.class)
                .like(AdminOperationLog::getAdminAccount, trim(query.getAdminAccount()),
                        StringUtils.hasText(query.getAdminAccount()))
                .like(AdminOperationLog::getAction, trim(query.getAction()), StringUtils.hasText(query.getAction()))
                .eq(AdminOperationLog::getSuccess, query.getSuccess(), query.getSuccess() != null)
                .ge(AdminOperationLog::getCreateTime, query.getStartTime(), query.getStartTime() != null)
                .le(AdminOperationLog::getCreateTime, query.getEndTime(), query.getEndTime() != null)
                .orderBy(AdminOperationLog::getId, false)
                .limit(limit);
        return operationLogMapper.selectListByQuery(wrapper).stream().map(this::toVo).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void record(HttpServletRequest request, int statusCode, long durationMs) {
        try {
            AdminOperationLog item = new AdminOperationLog();
            item.setAdminAccount(trim(UserContext.currentUsername(), 64));
            item.setMethod(trim(request.getMethod(), 16));
            item.setPath(trim(request.getRequestURI(), 512));
            item.setQueryString(trim(request.getQueryString(), 1024));
            item.setAction(resolveAction(request));
            item.setStatusCode(statusCode);
            item.setSuccess(statusCode >= 200 && statusCode < 400 ? 1 : 0);
            item.setDurationMs(durationMs);
            item.setIpAddress(trim(resolveIpAddress(request), 64));
            item.setUserAgent(trim(request.getHeader("User-Agent"), 512));
            operationLogMapper.insert(item);
        } catch (Exception ex) {
            log.warn("record admin operation log failed", ex);
        }
    }

    private String resolveAction(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (path.contains("/api/admin/content")) {
            return method + "_CONTENT_CONFIG";
        }
        if (path.contains("/api/admin/system-param")) {
            return method + "_SYSTEM_PARAM";
        }
        if (path.contains("/api/admin/user/status")) {
            return method + "_MEMBER_STATUS";
        }
        if (path.contains("/api/admin/user")) {
            return method + "_MEMBER_VIEW";
        }
        if (path.contains("/api/admin/withdraw")) {
            return method + "_WITHDRAW_REVIEW";
        }
        if (path.contains("/api/admin/recharge")) {
            return method + "_RECHARGE_VIEW";
        }
        if (path.contains("/api/admin/collect")) {
            return method + "_COLLECT_VIEW";
        }
        if (path.contains("/api/admin/wallet-flow")) {
            return method + "_WALLET_FLOW_VIEW";
        }
        if (path.contains("/api/admin/report")) {
            return method + "_REPORT_VIEW";
        }
        if (path.contains("/api/admin/operation-log")) {
            return method + "_OPERATION_LOG_VIEW";
        }
        if (path.contains("/api/sys/admin/logout")) {
            return method + "_ADMIN_LOGOUT";
        }
        return method + "_ADMIN_OPERATION";
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String trim(String value, int maxLength) {
        String trimmed = trim(value);
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private OperationLogVO toVo(AdminOperationLog item) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(item.getId());
        vo.setAdminAccount(item.getAdminAccount());
        vo.setMethod(item.getMethod());
        vo.setPath(item.getPath());
        vo.setQueryString(item.getQueryString());
        vo.setAction(item.getAction());
        vo.setStatusCode(item.getStatusCode());
        vo.setSuccess(item.getSuccess());
        vo.setDurationMs(item.getDurationMs());
        vo.setIpAddress(item.getIpAddress());
        vo.setUserAgent(item.getUserAgent());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }
}