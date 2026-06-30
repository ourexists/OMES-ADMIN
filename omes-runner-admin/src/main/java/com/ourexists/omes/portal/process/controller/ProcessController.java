/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.process.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.process.domain.BizProcess;
import com.ourexists.omes.process.model.*;
import com.ourexists.omes.process.service.BizProcessService;
import com.ourexists.omes.process.service.BizProcessStepWipService;
import com.ourexists.omes.process.support.ProcessFileAccessEnricher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "工艺卡片", description = "橡胶/塑料制件工艺卡片管理")
@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final BizProcessService processService;
    private final BizProcessStepWipService stepWipService;
    private final ProcessFileAccessEnricher fileAccessEnricher;

    @PostMapping("/page")
    @Operation(summary = "工艺分页列表")
    public JsonResponseEntity<java.util.List<ProcessVO>> page(@RequestBody ProcessPageQuery query) {
        Page<ProcessVO> voPage = processService.pageDetail(query.getPage(), query.getSize(), query.getKeyword());
        fileAccessEnricher.enrichProcessPage(voPage);
        return JsonResponseEntity.success(voPage.getRecords(), OrmUtils.extraPagination(voPage));
    }

    @PostMapping("/detail")
    @Operation(summary = "工艺详情")
    public JsonResponseEntity<ProcessVO> detail(@Valid @RequestBody ProcessIdRequest request) {
        ProcessVO vo = processService.detail(request.getId());
        fileAccessEnricher.enrichProcess(vo);
        return JsonResponseEntity.success(vo);
    }

    @PostMapping
    @Operation(summary = "创建工艺")
    public JsonResponseEntity<ProcessVO> create(@Valid @RequestBody ProcessSaveRequest request) {
        BizProcess process = processService.create(request);
        ProcessVO vo = processService.detail(process.getId());
        fileAccessEnricher.enrichProcess(vo);
        return JsonResponseEntity.success(vo);
    }

    @PostMapping("/update")
    @Operation(summary = "更新工艺")
    public JsonResponseEntity<ProcessVO> update(@Valid @RequestBody ProcessSaveRequest request) {
        BizProcess process = processService.update(request);
        ProcessVO vo = processService.detail(process.getId());
        fileAccessEnricher.enrichProcess(vo);
        return JsonResponseEntity.success(vo);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除工艺")
    public JsonResponseEntity<Boolean> delete(@Valid @RequestBody ProcessIdRequest request) {
        processService.delete(request);
        return JsonResponseEntity.success(true);
    }

    @PostMapping("/steps/update-script")
    @Operation(summary = "更新工序驱动引擎")
    public JsonResponseEntity<Boolean> updateStepScript(@Valid @RequestBody ProcessStepScriptUpdateRequest request) {
        processService.updateStepScript(request);
        return JsonResponseEntity.success(true);
    }

    @PostMapping("/steps/save")
    @Operation(summary = "保存工艺工序")
    public JsonResponseEntity<ProcessVO> saveProcessSteps(@Valid @RequestBody ProcessStepsSaveRequest request) {
        processService.saveProcessSteps(request);
        ProcessVO vo = processService.detail(request.getProcessId());
        fileAccessEnricher.enrichProcess(vo);
        return JsonResponseEntity.success(vo);
    }

    @PostMapping("/steps/wip/save")
    @Operation(summary = "保存工序 WIP/排产配置")
    public JsonResponseEntity<ProcessStepWipItem> saveStepWip(@Valid @RequestBody ProcessStepWipSaveRequest request) {
        return JsonResponseEntity.success(stepWipService.save(request));
    }
}
