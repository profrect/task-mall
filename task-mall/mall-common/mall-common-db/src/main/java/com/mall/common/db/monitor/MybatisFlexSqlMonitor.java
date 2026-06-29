package com.mall.common.db.monitor;

import com.mybatisflex.core.audit.AuditManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MybatisFlexSqlMonitor {

    /**
     * 慢SQL耗时阈值
     */
    private final Long slowSqlThreshold;

    public MybatisFlexSqlMonitor(Long slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
    }

    @PostConstruct
    public void init() {
        AuditManager.setAuditEnable(true);
        AuditManager.setMessageCollector(auditMessage -> {
            String fullSql = auditMessage.getFullSql();
            List<Object> queryParams = auditMessage.getQueryParams();
            long elapsedTime = auditMessage.getElapsedTime();
            if (elapsedTime > slowSqlThreshold) {
                log.warn("--->> [慢SQL告警]-[{}ms],[{}], 参数: {}", elapsedTime, fullSql, queryParams);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("--->> [SQL执行]-[{}ms]-[{}], 参数: {}", elapsedTime, fullSql, queryParams);
                }
            }
        });
    }
}
