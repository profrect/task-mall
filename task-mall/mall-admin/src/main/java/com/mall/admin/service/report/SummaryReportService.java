package com.mall.admin.service.report;

import com.mall.common.core.exception.BizException;
import com.mall.common.model.dto.resp.AdminSummaryReportResp;

/** 管理端综合报表：只聚合跨服务统计快照，不直接读取其它服务数据库。 */
public interface SummaryReportService {

    AdminSummaryReportResp summary() throws BizException;
}