package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.yomahub.liteflow.flow.FlowBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按工序脚本/配方编译结果注册各段 LiteFlow 链（{@link FlowBus#reloadChain}），无静态规则文件依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessLiteFlowChainRegistry {

    private final ProcessLiteFlowChainBuilder chainBuilder;
    private final Set<String> registeredChainIds = ConcurrentHashMap.newKeySet();

    public String resolveScriptKey(String stepId, String stepScript) {
        if (StringUtils.hasText(stepId)) {
            return sanitize(stepId);
        }
        if (!StringUtils.hasText(stepScript)) {
            return "anonymous";
        }
        return DigestUtils.md5DigestAsHex(stepScript.getBytes(StandardCharsets.UTF_8)).substring(0, 12);
    }

    public void registerScriptChains(String scriptKey, List<ProcessStepCombination> combinations) {
        if (combinations == null || combinations.isEmpty()) {
            return;
        }
        for (int i = 0; i < combinations.size(); i++) {
            ProcessStepCombination combo = combinations.get(i);
            String chainId = ProcessLiteFlowChainBuilder.chainId(scriptKey, i);
            String el = chainBuilder.buildSegmentEl(combo);
            combo.setChainId(chainId);
            combo.setLiteflowEl(el);
            reloadChain(chainId, el);
        }
        log.debug("已注册工序 LiteFlow 链 scriptKey={} segments={}", scriptKey, combinations.size());
    }

    public void reloadChain(String chainId, String el) {
        if (!StringUtils.hasText(chainId) || !StringUtils.hasText(el)) {
            return;
        }
        FlowBus.reloadChain(chainId, el);
        registeredChainIds.add(chainId);
    }

    public void hotReloadScript(String stepId, String stepScript, List<ProcessStepCombination> combinations) {
        registerScriptChains(resolveScriptKey(stepId, stepScript), combinations);
    }

    private static String sanitize(String stepId) {
        return stepId.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
