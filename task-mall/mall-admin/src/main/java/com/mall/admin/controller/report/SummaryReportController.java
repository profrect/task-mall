package com.mall.admin.controller.report;

import com.mall.admin.service.report.SummaryReportService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.model.dto.resp.AdminSummaryReportResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 综合报表（管理端）。只返回真实 user/wallet 数据，不填充任务、投资等未落地模块的假数据。 */
@RestController
@RequestMapping("/api/admin/report")
public class SummaryReportController {

    @Resource
    private SummaryReportService summaryReportService;

    @GetMapping("/summary")
    public Result<AdminSummaryReportResp> summary() throws BizException {
        return Result.ok(summaryReportService.summary());
    }
}