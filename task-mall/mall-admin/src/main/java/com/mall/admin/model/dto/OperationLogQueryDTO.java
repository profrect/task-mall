package com.mall.admin.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperationLogQueryDTO implements Serializable {
    private static final long serialVersionUID = 9115019579499019777L;

    private String adminAccount;
    private String action;
    private Integer success;
    private Long startTime;
    private Long endTime;
    private Integer limit;
}