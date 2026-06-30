package com.ourexists.omes.process.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.domain.BizProcessStepWip;
import com.ourexists.omes.process.model.ProcessStepWipItem;
import com.ourexists.omes.process.model.ProcessStepWipSaveRequest;
import com.ourexists.omes.process.mapper.BizProcessStepMapper;
import com.ourexists.omes.process.mapper.BizProcessStepWipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizProcessStepWipService {

    private final BizProcessStepWipMapper wipMapper;
    private final BizProcessStepMapper stepMapper;

    public Map<String, ProcessStepWipItem> mapItemsByStepNames(List<BizProcessStep> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyMap();
        }
        List<String> stepNames = steps.stream()
                .map(BizProcessStep::getStepName)
                .filter(StringUtils::hasText)
                .map(this::normalizeName)
                .distinct()
                .toList();
        if (stepNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, BizProcessStepWip> configByName = wipMapper.selectList(new LambdaQueryWrapper<BizProcessStepWip>()
                        .in(BizProcessStepWip::getStepName, stepNames))
                .stream()
                .filter(config -> StringUtils.hasText(config.getStepName()))
                .collect(Collectors.toMap(config -> normalizeName(config.getStepName()), item -> item, (left, right) -> left));
        Map<String, ProcessStepWipItem> result = new HashMap<>();
        for (BizProcessStep step : steps) {
            if (!StringUtils.hasText(step.getStepName())) {
                continue;
            }
            BizProcessStepWip config = configByName.get(normalizeName(step.getStepName()));
            if (config != null) {
                result.put(step.getId(), toItem(config));
            }
        }
        return result;
    }

    public BizProcessStepWip findByStepName(String stepName) {
        if (!StringUtils.hasText(stepName)) {
            return null;
        }
        return wipMapper.selectOne(new LambdaQueryWrapper<BizProcessStepWip>()
                .eq(BizProcessStepWip::getStepName, normalizeName(stepName))
                .last("LIMIT 1"));
    }

    public BizProcessStepWip findByStep(BizProcessStep step) {
        if (step == null || !StringUtils.hasText(step.getStepName())) {
            return null;
        }
        return findByStepName(step.getStepName());
    }

    public BizProcessStepWip findByStepId(String stepId) {
        if (!StringUtils.hasText(stepId)) {
            return null;
        }
        BizProcessStep step = stepMapper.selectById(stepId);
        return findByStep(step);
    }

    public Map<String, BizProcessStepWip> mapBySteps(List<BizProcessStep> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyMap();
        }
        List<String> stepNames = steps.stream()
                .map(BizProcessStep::getStepName)
                .filter(StringUtils::hasText)
                .map(this::normalizeName)
                .distinct()
                .toList();
        if (stepNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, BizProcessStepWip> configByName = wipMapper.selectList(new LambdaQueryWrapper<BizProcessStepWip>()
                        .in(BizProcessStepWip::getStepName, stepNames))
                .stream()
                .filter(config -> StringUtils.hasText(config.getStepName()))
                .collect(Collectors.toMap(config -> normalizeName(config.getStepName()), item -> item, (left, right) -> left));
        Map<String, BizProcessStepWip> result = new HashMap<>();
        for (BizProcessStep step : steps) {
            if (!StringUtils.hasText(step.getStepName())) {
                continue;
            }
            BizProcessStepWip config = configByName.get(normalizeName(step.getStepName()));
            if (config != null) {
                result.put(step.getId(), config);
            }
        }
        return result;
    }

    public List<BizProcessStep> listStepsByName(String stepName) {
        if (!StringUtils.hasText(stepName)) {
            return Collections.emptyList();
        }
        return stepMapper.selectList(new LambdaQueryWrapper<BizProcessStep>()
                .eq(BizProcessStep::getStepName, normalizeName(stepName))
                .orderByAsc(BizProcessStep::getSortOrder));
    }

    public List<BizProcessStepWip> listProduceConfigsByWipType(String wipType) {
        if (!StringUtils.hasText(wipType)) {
            return Collections.emptyList();
        }
        return wipMapper.selectList(new LambdaQueryWrapper<BizProcessStepWip>()
                .eq(BizProcessStepWip::getProduceWipFlag, 1)
                .eq(BizProcessStepWip::getWipType, wipType.trim())
                .orderByAsc(BizProcessStepWip::getCreatedTime));
    }

    @Transactional
    public ProcessStepWipItem save(ProcessStepWipSaveRequest request) {
        String stepName = normalizeName(request.getStepName());
        if (!StringUtils.hasText(stepName)) {
            throw new BusinessException("工序名称不能为空");
        }
        ProcessStepWipItem item = request.getWip();
        boolean produceWip = Boolean.TRUE.equals(item.getProduceWipFlag());
        boolean directTransfer = !produceWip && Boolean.TRUE.equals(item.getDirectTransferFlag());
        if (produceWip && !StringUtils.hasText(item.getWipType())) {
            throw new BusinessException("产出 WIP 的工序须配置 wipType");
        }
        if (produceWip && directTransfer) {
            throw new BusinessException("产出 WIP 与直送下一工序不能同时启用");
        }
        if (!produceWip && !directTransfer) {
            deleteByStepName(stepName);
            return null;
        }

        BizProcessStepWip config = findByStepName(stepName);
        if (config == null) {
            config = new BizProcessStepWip();
            config.setStepName(stepName);
        }
        config.setProduceWipFlag(produceWip ? 1 : 0);
        config.setDirectTransferFlag(directTransfer ? 1 : 0);
        config.setWipType(produceWip ? trimToNull(item.getWipType()) : null);
        config.setWipHoldTimeHours(produceWip ? item.getWipHoldTimeHours() : null);
        config.setScheduleDeviceCode((produceWip || directTransfer) ? trimToNull(item.getScheduleDeviceCode()) : null);
        config.setWipTriggerTargetStepName(resolveTargetStepName(stepName, item.getWipTriggerTargetStepName()));

        if (StringUtils.hasText(config.getId())) {
            wipMapper.updateById(config);
        } else {
            wipMapper.insert(config);
        }
        return toItem(config);
    }

    public BizProcessStep resolveSourceStep(BizProcessStepWip config, String processId) {
        if (config == null || !StringUtils.hasText(config.getStepName()) || !StringUtils.hasText(processId)) {
            return null;
        }
        return findStepByNameInProcess(processId, config.getStepName());
    }

    public BizProcessStep resolveTargetStep(BizProcessStepWip config, BizProcessStep sourceStep) {
        if (config == null || sourceStep == null || !StringUtils.hasText(sourceStep.getProcessId())) {
            return null;
        }
        if (StringUtils.hasText(config.getWipTriggerTargetStepName())) {
            return findStepByNameInProcess(sourceStep.getProcessId(), config.getWipTriggerTargetStepName());
        }
        return findNextStep(sourceStep);
    }

    public boolean producesWip(BizProcessStepWip config) {
        return config != null && config.getProduceWipFlag() != null && config.getProduceWipFlag() == 1;
    }

    public boolean directTransfer(BizProcessStepWip config) {
        return config != null && config.getDirectTransferFlag() != null && config.getDirectTransferFlag() == 1;
    }

    private BizProcessStep findStepByNameInProcess(String processId, String stepName) {
        if (!StringUtils.hasText(processId) || !StringUtils.hasText(stepName)) {
            return null;
        }
        return stepMapper.selectOne(new LambdaQueryWrapper<BizProcessStep>()
                .eq(BizProcessStep::getProcessId, processId)
                .eq(BizProcessStep::getStepName, normalizeName(stepName))
                .last("LIMIT 1"));
    }

    private BizProcessStep findNextStep(BizProcessStep sourceStep) {
        if (sourceStep == null || sourceStep.getSortOrder() == null) {
            return null;
        }
        return stepMapper.selectOne(new LambdaQueryWrapper<BizProcessStep>()
                .eq(BizProcessStep::getProcessId, sourceStep.getProcessId())
                .gt(BizProcessStep::getSortOrder, sourceStep.getSortOrder())
                .orderByAsc(BizProcessStep::getSortOrder)
                .last("LIMIT 1"));
    }

    private String resolveTargetStepName(String sourceStepName, String targetStepName) {
        if (!StringUtils.hasText(targetStepName)) {
            return null;
        }
        String normalized = normalizeName(targetStepName);
        if (normalized.equals(normalizeName(sourceStepName))) {
            throw new BusinessException("拉料目标工序不能与当前工序相同");
        }
        return normalized;
    }

    private ProcessStepWipItem toItem(BizProcessStepWip config) {
        if (config == null) {
            return null;
        }
        ProcessStepWipItem item = new ProcessStepWipItem();
        item.setProduceWipFlag(config.getProduceWipFlag() != null && config.getProduceWipFlag() == 1);
        item.setDirectTransferFlag(config.getDirectTransferFlag() != null && config.getDirectTransferFlag() == 1);
        item.setWipType(config.getWipType());
        item.setWipHoldTimeHours(config.getWipHoldTimeHours());
        item.setScheduleDeviceCode(config.getScheduleDeviceCode());
        item.setWipTriggerTargetStepName(config.getWipTriggerTargetStepName());
        return item;
    }

    private void deleteByStepName(String stepName) {
        wipMapper.delete(new LambdaQueryWrapper<BizProcessStepWip>()
                .eq(BizProcessStepWip::getStepName, normalizeName(stepName)));
    }

    private String normalizeName(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
