/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.process.controller;

import com.ourexists.omes.process.model.ProcessIdRequest;
import com.ourexists.omes.process.service.ProcessExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工艺卡片导出")
@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessExportController {

    private final ProcessExportService processExportService;

    @PostMapping("/export-pdf")
    @Operation(summary = "导出工艺卡片 PDF")
    public ResponseEntity<byte[]> exportPdf(@Valid @RequestBody ProcessIdRequest request) {
        return processExportService.exportPdf(request);
    }
}
