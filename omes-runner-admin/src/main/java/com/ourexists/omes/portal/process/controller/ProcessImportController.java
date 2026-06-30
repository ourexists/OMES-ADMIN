/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.process.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.process.model.ProcessImportParseResult;
import com.ourexists.omes.process.service.ProcessImportService;
import com.ourexists.omes.process.support.ProcessFileAccessEnricher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "工艺卡片导入")
@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessImportController {

    private final ProcessImportService processImportService;
    private final ProcessFileAccessEnricher fileAccessEnricher;

    @PostMapping("/import-pdf")
    @Operation(summary = "解析工艺卡片 PDF")
    public JsonResponseEntity<ProcessImportParseResult> importPdf(@RequestParam("file") MultipartFile file) {
        ProcessImportParseResult result = processImportService.parsePdf(file);
        fileAccessEnricher.enrichProcessImport(result);
        return JsonResponseEntity.success(result);
    }
}
