package com.mall.admin.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperationLogVO implements Serializable {
    private static final long serialVersionUID = -5590650470058937780L;

    private Long id;
    private String adminAccount;
    private String method;
    private String path;
    private String queryString;
    private String action;
    private Integer statusCode;
    private Integer success;
    private Long durationMs;
    private String ipAddress;
    private String userAgent;
    private Long createTime;
}