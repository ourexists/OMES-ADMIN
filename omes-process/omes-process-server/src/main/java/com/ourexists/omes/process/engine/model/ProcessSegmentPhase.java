package com.ourexists.omes.process.engine.model;

/**
 * 工序段内阶段类型，运行顺序由流程图主链 {@link ProcessStepCombination#getPhaseOrder()} 决定。
 */
public enum ProcessSegmentPhase {

    DRIVE,
    ACTION,
    COMPLETE;

    public static ProcessSegmentPhase fromText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("阶段类型为空");
        }
        return ProcessSegmentPhase.valueOf(text.trim().toUpperCase());
    }
}
