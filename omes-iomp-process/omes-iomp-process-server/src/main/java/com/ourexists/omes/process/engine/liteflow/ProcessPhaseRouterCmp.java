package com.ourexists.omes.process.engine.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeSwitchComponent;
import lombok.RequiredArgsConstructor;

@LiteflowComponent("processPhaseRouter")
@RequiredArgsConstructor
public class ProcessPhaseRouterCmp extends NodeSwitchComponent {

    private final ProcessLiteFlowSupport support;

    @Override
    public String processSwitch() {
        return support.slot(this).getPhase().nodeId();
    }
}
