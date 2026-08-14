package com.ourexists.omes.portal.line.service;

import com.ourexists.omes.line.model.TFDto;
import com.ourexists.omes.line.model.TfEquipmentRef;
import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.engine.ProcessStepScriptCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 产线工序规则引擎：编译 stepScript / stepEngineConfig。
 */
@Service
@RequiredArgsConstructor
public class TfStepEngineService {

    private final ProcessStepScriptCodec stepScriptCodec;

    public void applyEngineConfig(TFDto dto) {
        if (dto == null) {
            return;
        }
        String stepScript = trimToNull(dto.getStepScript());
        dto.setStepScript(stepScript);
        if (!StringUtils.hasText(stepScript)) {
            dto.setStepEngineConfig(null);
            return;
        }
        String stepId = StringUtils.hasText(dto.getId()) ? dto.getId().trim() : dto.getSelfCode();
        stepScriptCodec.validateStepScript(stepScript, stepId);
        stepScriptCodec.hotReloadStepScript(stepId, stepScript);
        BizProcessStep step = new BizProcessStep();
        step.setId(stepId);
        step.setStepName(dto.getName());
        step.setStepScript(stepScript);
        dto.setStepEngineConfig(stepScriptCodec.compileEngineConfig(step, resolvePrimaryEquipmentCode(dto)));
    }

    private String resolvePrimaryEquipmentCode(TFDto dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getEquipments())) {
            return null;
        }
        for (TfEquipmentRef ref : dto.getEquipments()) {
            if (ref != null && StringUtils.hasText(ref.getEquipmentCode())) {
                return ref.getEquipmentCode().trim();
            }
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
