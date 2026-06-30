package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import org.springframework.stereotype.Component;

/**
 * 为每个工序段生成 LiteFlow EL，并通过 {@link ProcessLiteFlowChainRegistry} 动态注册。
 * <p>
 * 不再使用静态 {@code *.el.xml}；EL 模板仅维护于此处。
 */
@Component
public class ProcessLiteFlowChainBuilder {

    public static final String STANDARD_SEGMENT_EL = """
            THEN(
                processExceptionCheck,
                SWITCH(processPhaseRouter).to(processDriveWait, processActionTick, processCompleteWait)
            )""".replace('\n', ' ').trim();

    public static final String AUTO_NEXT_SEGMENT_EL = """
            THEN(
                processExceptionCheck,
                SWITCH(processPhaseRouter).to(processDriveWait, processActionTick)
            )""".replace('\n', ' ').trim();

    public String buildSegmentEl(ProcessStepCombination combo) {
        if (combo.getComplete() != null
                && combo.getComplete().resolvedKind() == ProcessConditionKind.AUTO_NEXT) {
            return AUTO_NEXT_SEGMENT_EL;
        }
        return STANDARD_SEGMENT_EL;
    }

    public static String chainId(String scriptKey, int segmentIndex) {
        return "ps_" + scriptKey + "_seg" + segmentIndex;
    }
}
