package com.ourexists.omes.ai.report.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ai.model.AiInspectionReportRequest;
import com.ourexists.omes.ai.model.AiInspectionReportResponse;
import com.ourexists.omes.ai.report.service.AiInspectionReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI巡检报告-报告子模块")
@RestController
@RequestMapping("/inspection/ai")
public class AiInspectionReportController {

    private final AiInspectionReportService reportService;

    public AiInspectionReportController(AiInspectionReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "生成AI巡检报告")
    @PostMapping("/report")
    public JsonResponseEntity<AiInspectionReportResponse> generateReport(@RequestBody(required = false) AiInspectionReportRequest request) {
        AiInspectionReportRequest actualRequest = request == null ? new AiInspectionReportRequest() : request;
        return JsonResponseEntity.success(reportService.generateReport(actualRequest));
    }
}
