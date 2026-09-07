package com.ourexists.omes.process.engine.liteflow;

/**
 * 工序组合引擎 LiteFlow 阶段（与 SWITCH 路由 tag 一致）。
 */
public enum ProcessComboPhase {

    WAIT_DRIVE("processDriveWait"),
    RUN_ACTION("processActionTick"),
    WAIT_COMPLETE("processCompleteWait");

    private final String nodeId;

    ProcessComboPhase(String nodeId) {
        this.nodeId = nodeId;
    }

    public String nodeId() {
        return nodeId;
    }

    public static ProcessComboPhase fromSegmentPhase(com.ourexists.omes.process.engine.model.ProcessSegmentPhase phase) {
        return switch (phase) {
            case DRIVE -> WAIT_DRIVE;
            case ACTION -> RUN_ACTION;
            case COMPLETE -> WAIT_COMPLETE;
        };
    }
}
