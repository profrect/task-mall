package com.mall.admin.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/** 后台操作审计日志。 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "admin_operation_log", onInsert = CustomInsertListener.class)
public class AdminOperationLog extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 3133145615324757525L;

    private String adminAccount;
    private String method;
    private String path;
    private String queryString;
    private String action;
    private Integer statusCode;
    private Integer success;
    private Long durationMs;
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;
}