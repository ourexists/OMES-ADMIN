package com.ourexists.omes.process.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.domain.*;
import com.ourexists.omes.process.model.*;
import com.ourexists.omes.process.engine.ProcessStepScriptCodec;
import com.ourexists.omes.process.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizProcessService {

    private final BizProcessMapper processMapper;
    private final BizProcessMoldMapper moldMapper;
    private final BizProcessStepMapper stepMapper;
    private final BizProcessStepEquipmentMapper equipmentMapper;
    private final BizProcessStepToolingMapper toolingMapper;
    private final ProcessStepScriptCodec stepScriptCodec;
    private final BizProcessStepWipService stepWipService;

    public Page<BizProcess> page(int page, int size, String keyword) {
        LambdaQueryWrapper<BizProcess> wrapper = new LambdaQueryWrapper<BizProcess>()
                .orderByDesc(BizProcess::getCreatedTime);
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(BizProcess::getProcessCode, kw)
                    .or().like(BizProcess::getProcessName, kw));
        }
        return processMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Page<ProcessVO> pageDetail(int page, int size, String keyword) {
        Page<BizProcess> result = page(page, size, keyword);
        List<String> processIds = result.getRecords().stream().map(BizProcess::getId).toList();
        Map<String, List<ProcessMoldItem>> moldsMap = batchLoadMolds(processIds);
        Map<String, List<ProcessStepItem>> stepsMap = batchLoadSteps(processIds);

        Page<ProcessVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(process -> {
            ProcessVO vo = toDetailVO(process);
            vo.setMolds(moldsMap.getOrDefault(process.getId(), Collections.emptyList()));
            vo.setSteps(stepsMap.getOrDefault(process.getId(), Collections.emptyList()));
            return vo;
        }).toList());
        return voPage;
    }

    public ProcessVO detail(String id) {
        BizProcess process = requireProcess(id);
        ProcessVO vo = toDetailVO(process);
        vo.setMolds(loadMolds(id));
        vo.setSteps(loadSteps(id));
        return vo;
    }

    @Transactional
    public BizProcess create(ProcessSaveRequest request) {
        ensureProcessCodeUnique(request.getProcessCode(), null);
        BizProcess process = new BizProcess();
        copyMainFields(process, request);
        processMapper.insert(process);
        saveChildren(process.getId(), request, Collections.emptyMap());
        return process;
    }

    @Transactional
    public BizProcess update(ProcessSaveRequest request) {
        if (!StringUtils.hasText(request.getId())) {
            throw new BusinessException("工艺ID不能为空");
        }
        BizProcess process = requireProcess(request.getId());
        ensureProcessCodeUnique(request.getProcessCode(), process.getId());
        copyMainFields(process, request);
        processMapper.updateById(process);
        deleteMolds(process.getId());
        saveMolds(process.getId(), request.getMolds());
        if (request.getSteps() != null) {
            Map<Integer, String> existingScripts = loadStepScriptsByStepNo(process.getId());
            Map<Integer, String> existingParams = loadStepParamsByStepNo(process.getId());
            deleteSteps(process.getId());
            saveSteps(process.getId(), request.getSteps(), existingScripts, existingParams);
        }
        return process;
    }

    @Transactional
    public void saveProcessSteps(ProcessStepsSaveRequest request) {
        requireProcess(request.getProcessId());
        Map<Integer, String> existingScripts = loadStepScriptsByStepNo(request.getProcessId());
        Map<Integer, String> existingParams = loadStepParamsByStepNo(request.getProcessId());
        deleteSteps(request.getProcessId());
        saveSteps(request.getProcessId(), request.getSteps(), existingScripts, existingParams);
    }

    @Transactional
    public void updateStepScript(ProcessStepScriptUpdateRequest request) {
        BizProcessStep step = stepMapper.selectById(request.getStepId());
        if (step == null) {
            throw new BusinessException(404, "工序不存在");
        }
        String stepScript = trimToNull(request.getStepScript());
        step.setStepScript(stepScript);
        if (request.getParams() != null) {
            step.setParams(trimToNull(request.getParams()));
        }
        applyStepEngineConfig(step, loadPrimaryEquipmentCode(step.getId()));
        stepMapper.updateById(step);
    }

    @Transactional
    public void delete(ProcessIdRequest request) {
        requireProcess(request.getId());
        deleteChildren(request.getId());
        processMapper.deleteById(request.getId());
    }

    public ProcessListVO toListVO(BizProcess process) {
        return toDetailVO(process);
    }

    public ProcessVO toDetailVO(BizProcess process) {
        ProcessVO vo = new ProcessVO();
        fillMainFields(vo, process);
        return vo;
    }

    private void fillMainFields(ProcessListVO vo, BizProcess process) {
        vo.setId(process.getId());
        vo.setProcessCode(process.getProcessCode());
        vo.setProcessName(process.getProcessName());
        vo.setProcessImageUrl(process.getProcessImageUrl());
        vo.setProductCode(process.getProductCode());
        vo.setProductName(process.getProductName());
        vo.setComponentCode(process.getComponentCode());
        vo.setComponentName(process.getComponentName());
        vo.setMaterialCode(process.getMaterialCode());
        vo.setMaterialName(process.getMaterialName());
        vo.setTechCondition(process.getTechCondition());
        vo.setMaterialPreheat(process.getMaterialPreheat());
        vo.setPressPressure(process.getPressPressure());
        vo.setBlankWeight(process.getBlankWeight());
        vo.setBlankWeightUpperOffset(process.getBlankWeightUpperOffset());
        vo.setBlankWeightLowerOffset(process.getBlankWeightLowerOffset());
        vo.setPressTemperature(process.getPressTemperature());
        vo.setPressTemperatureUpperOffset(process.getPressTemperatureUpperOffset());
        vo.setPressTemperatureLowerOffset(process.getPressTemperatureLowerOffset());
        vo.setHoldTimeSeconds(process.getHoldTimeSeconds());
        vo.setCreateTime(process.getCreatedTime());
        vo.setUpdateTime(process.getUpdatedTime());
    }

    private void copyMainFields(BizProcess process, ProcessSaveRequest request) {
        process.setProcessCode(request.getProcessCode().trim());
        process.setProcessImageUrl(resolveProcessImageForStore(request.getProcessImageUrl()));
        process.setProcessName(request.getProcessName().trim());
        process.setProductCode(trimToNull(request.getProductCode()));
        process.setProductName(trimToNull(request.getProductName()));
        process.setComponentCode(trimToNull(request.getComponentCode()));
        process.setComponentName(trimToNull(request.getComponentName()));
        process.setMaterialCode(trimToNull(request.getMaterialCode()));
        process.setMaterialName(trimToNull(request.getMaterialName()));
        process.setTechCondition(trimToNull(request.getTechCondition()));
        process.setMaterialPreheat(trimToNull(request.getMaterialPreheat()));
        process.setPressPressure(request.getPressPressure());
        process.setBlankWeight(request.getBlankWeight());
        process.setBlankWeightUpperOffset(request.getBlankWeightUpperOffset());
        process.setBlankWeightLowerOffset(request.getBlankWeightLowerOffset());
        process.setPressTemperature(request.getPressTemperature());
        process.setPressTemperatureUpperOffset(request.getPressTemperatureUpperOffset());
        process.setPressTemperatureLowerOffset(request.getPressTemperatureLowerOffset());
        process.setHoldTimeSeconds(request.getHoldTimeSeconds());
    }

    private Map<Integer, String> loadStepScriptsByStepNo(String processId) {
        List<BizProcessStep> steps = stepMapper.selectList(
                new LambdaQueryWrapper<BizProcessStep>().eq(BizProcessStep::getProcessId, processId));
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyMap();
        }
        Map<Integer, String> scripts = new HashMap<>();
        for (BizProcessStep step : steps) {
            if (StringUtils.hasText(step.getStepScript()) && step.getStepNo() != null) {
                scripts.put(step.getStepNo(), step.getStepScript());
            }
        }
        return scripts;
    }

    private Map<Integer, String> loadStepParamsByStepNo(String processId) {
        List<BizProcessStep> steps = stepMapper.selectList(
                new LambdaQueryWrapper<BizProcessStep>().eq(BizProcessStep::getProcessId, processId));
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyMap();
        }
        Map<Integer, String> paramsMap = new HashMap<>();
        for (BizProcessStep step : steps) {
            if (StringUtils.hasText(step.getParams()) && step.getStepNo() != null) {
                paramsMap.put(step.getStepNo(), step.getParams());
            }
        }
        return paramsMap;
    }

    private void saveChildren(String processId, ProcessSaveRequest request, Map<Integer, String> existingScripts) {
        saveMolds(processId, request.getMolds());
        saveSteps(processId, request.getSteps(), existingScripts, Collections.emptyMap());
    }

    private void saveMolds(String processId, List<ProcessMoldItem> molds) {
        if (CollectionUtils.isEmpty(molds)) {
            return;
        }
        int order = 0;
        for (ProcessMoldItem item : molds) {
            if (!StringUtils.hasText(item.getMoldDrawingNo())) {
                continue;
            }
            BizProcessMold mold = new BizProcessMold();
            mold.setProcessId(processId);
            mold.setMoldDrawingNo(item.getMoldDrawingNo().trim());
            mold.setSlotCount(item.getSlotCount() != null ? item.getSlotCount() : 0);
            mold.setSortOrder(order++);
            moldMapper.insert(mold);
        }
    }

    private void saveSteps(String processId,
                           List<ProcessStepItem> steps,
                           Map<Integer, String> existingScripts,
                           Map<Integer, String> existingParams) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        int order = 0;
        for (ProcessStepItem item : steps) {
            if (!StringUtils.hasText(item.getStepName())) {
                continue;
            }
            BizProcessStep step = new BizProcessStep();
            step.setProcessId(processId);
            step.setStepNo(resolveStepNo(item, order));
            step.setStepCode(resolveStepCode(item, order));
            step.setStepName(item.getStepName().trim());
            step.setStepContent(trimToNull(item.getStepContent()));
            String stepScript = trimToNull(item.getStepScript());
            if (stepScript == null && existingScripts != null) {
                stepScript = existingScripts.get(resolveStepNo(item, order));
            }
            step.setStepScript(stepScript);
            String params = trimToNull(item.getParams());
            if (params == null && existingParams != null) {
                params = existingParams.get(resolveStepNo(item, order));
            }
            step.setParams(params);
            step.setSortOrder(order++);
            stepMapper.insert(step);
            saveStepRelations(step.getId(), item);
            applyStepEngineConfig(step, resolvePrimaryEquipmentCode(item));
            stepMapper.updateById(step);
        }
    }

    private void applyStepEngineConfig(BizProcessStep step, String equipmentCode) {
        if (StringUtils.hasText(step.getStepScript())) {
            stepScriptCodec.validateStepScript(step.getStepScript(), step.getId());
            stepScriptCodec.hotReloadStepScript(step.getId(), step.getStepScript());
        }
        step.setStepEngineConfig(stepScriptCodec.compileEngineConfig(step, equipmentCode));
    }

    private String resolvePrimaryEquipmentCode(ProcessStepItem item) {
        if (item == null || CollectionUtils.isEmpty(item.getEquipments())) {
            return null;
        }
        for (ProcessEquipmentRef ref : item.getEquipments()) {
            if (StringUtils.hasText(ref.getEquipmentCode())) {
                return ref.getEquipmentCode().trim();
            }
        }
        return null;
    }

    private String loadPrimaryEquipmentCode(String stepId) {
        List<BizProcessStepEquipment> list = equipmentMapper.selectList(
                new LambdaQueryWrapper<BizProcessStepEquipment>()
                        .eq(BizProcessStepEquipment::getStepId, stepId)
                        .orderByAsc(BizProcessStepEquipment::getCreatedTime));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0).getEquipmentCode();
    }

    private void saveStepRelations(String stepId, ProcessStepItem item) {
        if (!CollectionUtils.isEmpty(item.getEquipments())) {
            for (ProcessEquipmentRef ref : item.getEquipments()) {
                if (!StringUtils.hasText(ref.getEquipmentCode())) {
                    continue;
                }
                BizProcessStepEquipment eq = new BizProcessStepEquipment();
                eq.setStepId(stepId);
                eq.setEquipmentCode(ref.getEquipmentCode().trim());
                eq.setEquipmentName(StringUtils.hasText(ref.getEquipmentName())
                        ? ref.getEquipmentName().trim() : ref.getEquipmentCode().trim());
                equipmentMapper.insert(eq);
            }
        }
        if (!CollectionUtils.isEmpty(item.getToolings())) {
            for (ProcessToolingRef ref : item.getToolings()) {
                if (!StringUtils.hasText(ref.getToolingCode())) {
                    continue;
                }
                BizProcessStepTooling tooling = new BizProcessStepTooling();
                tooling.setStepId(stepId);
                tooling.setToolingCode(ref.getToolingCode().trim());
                tooling.setToolingName(StringUtils.hasText(ref.getToolingName())
                        ? ref.getToolingName().trim() : ref.getToolingCode().trim());
                toolingMapper.insert(tooling);
            }
        }
    }

    private void deleteChildren(String processId) {
        deleteSteps(processId);
        deleteMolds(processId);
    }

    private void deleteSteps(String processId) {
        List<BizProcessStep> steps = stepMapper.selectList(
                new LambdaQueryWrapper<BizProcessStep>().eq(BizProcessStep::getProcessId, processId));
        if (!CollectionUtils.isEmpty(steps)) {
            List<String> stepIds = steps.stream().map(BizProcessStep::getId).toList();
            equipmentMapper.delete(new LambdaQueryWrapper<BizProcessStepEquipment>()
                    .in(BizProcessStepEquipment::getStepId, stepIds));
            toolingMapper.delete(new LambdaQueryWrapper<BizProcessStepTooling>()
                    .in(BizProcessStepTooling::getStepId, stepIds));
        }
        stepMapper.delete(new LambdaQueryWrapper<BizProcessStep>()
                .eq(BizProcessStep::getProcessId, processId));
    }

    private void deleteMolds(String processId) {
        moldMapper.delete(new LambdaQueryWrapper<BizProcessMold>()
                .eq(BizProcessMold::getProcessId, processId));
    }

    private List<ProcessMoldItem> loadMolds(String processId) {
        return batchLoadMolds(List.of(processId)).getOrDefault(processId, Collections.emptyList());
    }

    private List<ProcessStepItem> loadSteps(String processId) {
        return batchLoadSteps(List.of(processId)).getOrDefault(processId, Collections.emptyList());
    }

    private Map<String, List<ProcessMoldItem>> batchLoadMolds(List<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) {
            return Collections.emptyMap();
        }
        List<BizProcessMold> molds = moldMapper.selectList(
                new LambdaQueryWrapper<BizProcessMold>()
                        .in(BizProcessMold::getProcessId, processIds)
                        .orderByAsc(BizProcessMold::getSortOrder));
        if (CollectionUtils.isEmpty(molds)) {
            return Collections.emptyMap();
        }
        return molds.stream().collect(Collectors.groupingBy(
                BizProcessMold::getProcessId,
                Collectors.mapping(m -> {
                    ProcessMoldItem item = new ProcessMoldItem();
                    item.setMoldDrawingNo(m.getMoldDrawingNo());
                    item.setSlotCount(m.getSlotCount());
                    return item;
                }, Collectors.toList())));
    }

    private Map<String, List<ProcessStepItem>> batchLoadSteps(List<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) {
            return Collections.emptyMap();
        }
        List<BizProcessStep> steps = stepMapper.selectList(
                new LambdaQueryWrapper<BizProcessStep>()
                        .in(BizProcessStep::getProcessId, processIds)
                        .orderByAsc(BizProcessStep::getSortOrder));
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyMap();
        }
        List<String> stepIds = steps.stream().map(BizProcessStep::getId).toList();
        Map<String, List<ProcessEquipmentRef>> equipMap = loadEquipments(stepIds);
        Map<String, List<ProcessToolingRef>> toolingMap = loadToolings(stepIds);
        Map<String, ProcessStepWipItem> wipByStepId = stepWipService.mapItemsByStepNames(steps);
        Map<String, List<ProcessStepItem>> result = new HashMap<>();
        for (BizProcessStep step : steps) {
            ProcessStepItem item = new ProcessStepItem();
            item.setId(step.getId());
            item.setStepNo(step.getStepNo());
            item.setStepCode(step.getStepCode());
            item.setStepName(step.getStepName());
            item.setWip(wipByStepId.get(step.getId()));
            item.setStepContent(step.getStepContent());
            item.setStepScript(step.getStepScript());
            item.setParams(step.getParams());
            item.setStepEngineConfig(step.getStepEngineConfig());
            item.setEquipments(equipMap.getOrDefault(step.getId(), Collections.emptyList()));
            item.setToolings(toolingMap.getOrDefault(step.getId(), Collections.emptyList()));
            result.computeIfAbsent(step.getProcessId(), ignored -> new ArrayList<>()).add(item);
        }
        return result;
    }

    private Map<String, List<ProcessEquipmentRef>> loadEquipments(List<String> stepIds) {
        List<BizProcessStepEquipment> list = equipmentMapper.selectList(
                new LambdaQueryWrapper<BizProcessStepEquipment>()
                        .in(BizProcessStepEquipment::getStepId, stepIds));
        return list.stream().collect(Collectors.groupingBy(
                BizProcessStepEquipment::getStepId,
                Collectors.mapping(e -> {
                    ProcessEquipmentRef ref = new ProcessEquipmentRef();
                    ref.setEquipmentCode(e.getEquipmentCode());
                    ref.setEquipmentName(e.getEquipmentName());
                    return ref;
                }, Collectors.toList())));
    }

    private Map<String, List<ProcessToolingRef>> loadToolings(List<String> stepIds) {
        List<BizProcessStepTooling> list = toolingMapper.selectList(
                new LambdaQueryWrapper<BizProcessStepTooling>()
                        .in(BizProcessStepTooling::getStepId, stepIds));
        return list.stream().collect(Collectors.groupingBy(
                BizProcessStepTooling::getStepId,
                Collectors.mapping(t -> {
                    ProcessToolingRef ref = new ProcessToolingRef();
                    ref.setToolingCode(t.getToolingCode());
                    ref.setToolingName(t.getToolingName());
                    return ref;
                }, Collectors.toList())));
    }

    private BizProcess requireProcess(String id) {
        BizProcess process = processMapper.selectById(id);
        if (process == null) {
            throw new BusinessException(404, "工艺不存在");
        }
        return process;
    }

    private void ensureProcessCodeUnique(String processCode, String excludeId) {
        LambdaQueryWrapper<BizProcess> wrapper = new LambdaQueryWrapper<BizProcess>()
                .eq(BizProcess::getProcessCode, processCode.trim());
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(BizProcess::getId, excludeId);
        }
        if (processMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("工艺编号已存在");
        }
    }

    private Integer resolveStepNo(ProcessStepItem item, int sortOrder) {
        if (item.getStepNo() != null && item.getStepNo() > 0) {
            return item.getStepNo();
        }
        return (sortOrder + 1) * 5;
    }

    private String resolveStepCode(ProcessStepItem item, int sortOrder) {
        if (StringUtils.hasText(item.getStepCode())) {
            return item.getStepCode().trim();
        }
        if (StringUtils.hasText(item.getStepName())) {
            return item.getStepName().trim();
        }
        return "STEP_" + resolveStepNo(item, sortOrder);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveProcessImageForStore(String image) {
        if (!StringUtils.hasText(image)) {
            return null;
        }
        String value = image.trim();
        if (!isValidProcessImageStoragePath(value)) {
            throw new BusinessException("工艺简图须保存为文件存储路径，不支持外链或 Base64");
        }
        return value;
    }

    private boolean isValidProcessImageStoragePath(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        String value = path.trim();
        if (value.startsWith("data:") || value.contains("base64,")) {
            return false;
        }
        if (value.startsWith("blob:") || value.startsWith("http://") || value.startsWith("https://")) {
            return false;
        }
        if (value.contains("..") || value.length() > 512) {
            return false;
        }
        return !value.startsWith("/");
    }
}
