package com.ourexists.omes.process.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.domain.BizProcess;
import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.model.*;
import com.ourexists.omes.process.engine.ProcessSequenceRunner;
import com.ourexists.omes.process.engine.ProcessStepScriptCodec;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepScript;
import com.ourexists.omes.process.engine.model.ProcessStepSimulationSession;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.spi.InMemoryProcessSignalProvider;
import com.ourexists.omes.process.mapper.BizProcessMapper;
import com.ourexists.omes.process.mapper.BizProcessStepMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ProcessStepSimulationService {

    private static final int MAX_SESSIONS = 100;

    private final ProcessStepScriptCodec scriptCodec;
    private final ProcessSequenceRunner sequenceRunner;
    private final BizProcessMapper processMapper;
    private final BizProcessStepMapper stepMapper;
    private final ProcessStepExecutionService executionService;
    private final ProcessRecipeService recipeService;

    private final Map<String, ProcessStepSimulationSession> sessions = new ConcurrentHashMap<>();

    public ProcessStepSimulationStartVO start(ProcessStepSimulationStartRequest request) {
        boolean hasProcessId = StringUtils.hasText(request.getProcessId());
        boolean hasScript = StringUtils.hasText(request.getScriptJson());
        boolean hasRecipe = StringUtils.hasText(request.getRecipeId());
        int modeCount = (hasProcessId ? 1 : 0) + (hasScript ? 1 : 0) + (hasRecipe ? 1 : 0);
        if (modeCount != 1) {
            throw new BusinessException("请指定 processId、scriptJson 或 recipeId 其中之一");
        }
        if (sessions.size() >= MAX_SESSIONS) {
            throw new BusinessException("仿真会话已达上限，请先停止无用会话");
        }

        ProcessStepScript script;
        String processId = hasProcessId ? request.getProcessId().trim() : null;
        if (hasRecipe) {
            script = recipeService.buildScript(request.getRecipeId().trim(), request.getEquipmentCode());
        } else if (hasScript) {
            script = scriptCodec.parseScript(request.getScriptJson());
        } else {
            requireProcess(processId);
            script = scriptCodec.buildScriptFromSteps(loadOrderedSteps(processId));
        }

        InMemoryProcessSignalProvider signals = new InMemoryProcessSignalProvider();
        applySignals(signals, request.getInitialTemperature(), request.getInitialStates(), null);

        ProcessSequenceRunner.RunningSequence sequence = sequenceRunner.start(script, signals);
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        ProcessStepSimulationSession session = new ProcessStepSimulationSession(
                sessionId, processId, script, signals, sequence);
        sessions.put(sessionId, session);
        return toStartVO(session);
    }

    public ProcessStepSimulationTickVO tick(ProcessStepSimulationTickRequest request) {
        ProcessStepSimulationSession session = requireSession(request.getSessionId());
        long deltaMs = request.getDeltaMs() != null && request.getDeltaMs() > 0
                ? request.getDeltaMs()
                : 1000L;
        ProcessStepTickResult result = executionService.tick(session.getSequence(), deltaMs);
        session.setLastTickAt(Instant.now());
        return toTickVO(session, result);
    }

    public void updateSignals(ProcessStepSimulationSignalsRequest request) {
        ProcessStepSimulationSession session = requireSession(request.getSessionId());
        applySignals(session.getSignalProvider(), request.getTemperature(), request.getStates(),
                request.getCompleteConfirmed());
    }

    public ProcessStepSimulationStatusVO status(String sessionId) {
        return toStatusVO(requireSession(sessionId));
    }

    public void stop(String sessionId) {
        ProcessStepSimulationSession session = sessions.remove(sessionId.trim());
        if (session != null) {
            session.getSequence().getActiveEngine().cancel();
        }
    }

    private ProcessStepSimulationSession requireSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException("sessionId 不能为空");
        }
        ProcessStepSimulationSession session = sessions.get(sessionId.trim());
        if (session == null) {
            throw new BusinessException(404, "仿真会话不存在或已结束");
        }
        return session;
    }

    private List<BizProcessStep> loadOrderedSteps(String processId) {
        return stepMapper.selectList(new LambdaQueryWrapper<BizProcessStep>()
                .eq(BizProcessStep::getProcessId, processId)
                .orderByAsc(BizProcessStep::getSortOrder)
                .orderByAsc(BizProcessStep::getStepNo));
    }

    private void requireProcess(String processId) {
        BizProcess process = processMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException(404, "工艺不存在");
        }
    }

    private void applySignals(InMemoryProcessSignalProvider signals,
                              Double temperature,
                              Map<String, Boolean> states,
                              Boolean completeConfirmed) {
        if (temperature != null) {
            signals.setTemperature(temperature);
        }
        if (states != null) {
            states.forEach((token, active) -> {
                if (StringUtils.hasText(token) && active != null) {
                    signals.setState(token, active);
                }
            });
        }
        if (completeConfirmed != null) {
            signals.setCompleteConfirmed(completeConfirmed);
        }
    }

    private ProcessStepSimulationStartVO toStartVO(ProcessStepSimulationSession session) {
        ProcessStepDefinition current = currentStep(session);
        return ProcessStepSimulationStartVO.builder()
                .sessionId(session.getSessionId())
                .processId(session.getProcessId())
                .totalSteps(session.getScript().getSteps().size())
                .currentStepIndex(session.getSequence().getStepIndex())
                .currentStepType(current.getType())
                .currentStepMode(current.getMode())
                .build();
    }

    private ProcessStepSimulationTickVO toTickVO(ProcessStepSimulationSession session,
                                                 ProcessStepTickResult result) {
        ProcessSequenceRunner.RunningSequence sequence = session.getSequence();
        return ProcessStepSimulationTickVO.builder()
                .sessionId(session.getSessionId())
                .phase(result.getPhase())
                .message(result.getMessage())
                .commandedValue(result.getCommandedValue())
                .actualTemperature(session.getSignalProvider().readTemperature())
                .currentStepIndex(sequence.getStepIndex())
                .totalSteps(session.getScript().getSteps().size())
                .finished(sequence.isFinished())
                .stepElapsedMs(sequence.getContext().getStepElapsedMs())
                .build();
    }

    private ProcessStepSimulationStatusVO toStatusVO(ProcessStepSimulationSession session) {
        ProcessStepDefinition current = currentStep(session);
        ProcessSequenceRunner.RunningSequence sequence = session.getSequence();
        return ProcessStepSimulationStatusVO.builder()
                .sessionId(session.getSessionId())
                .processId(session.getProcessId())
                .currentStepIndex(sequence.getStepIndex())
                .totalSteps(session.getScript().getSteps().size())
                .currentStepType(current.getType())
                .currentStepMode(current.getMode())
                .commandedValue(sequence.getContext().getCommandedSetpoint())
                .actualTemperature(session.getSignalProvider().readTemperature())
                .finished(sequence.isFinished())
                .createdAt(session.getCreatedAt())
                .lastTickAt(session.getLastTickAt())
                .build();
    }

    private ProcessStepDefinition currentStep(ProcessStepSimulationSession session) {
        int index = session.getSequence().getStepIndex();
        return session.getScript().getSteps().get(index);
    }
}
