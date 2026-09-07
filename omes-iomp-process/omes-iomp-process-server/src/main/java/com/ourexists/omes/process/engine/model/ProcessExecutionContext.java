package com.ourexists.omes.process.engine.model;

import com.ourexists.omes.process.engine.spi.ProcessSignalProvider;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 单次工艺运行上下文，在步骤引擎之间传递设定值与稳定性观测数据。
 */
@Getter
public class ProcessExecutionContext {

    private final ProcessSignalProvider signalProvider;
    private final Map<String, Object> attributes = new HashMap<>();

    private double commandedSetpoint;
    private Instant stepStartedAt;
    private long stepElapsedMs;

    public ProcessExecutionContext(ProcessSignalProvider signalProvider) {
        this.signalProvider = signalProvider;
        this.commandedSetpoint = signalProvider.readTemperature();
    }

    public void markStepStarted(Instant startedAt) {
        this.stepStartedAt = startedAt;
        this.stepElapsedMs = 0L;
    }

    public void addElapsed(long deltaMs) {
        this.stepElapsedMs += Math.max(0L, deltaMs);
    }

    public void setCommandedSetpoint(double commandedSetpoint) {
        this.commandedSetpoint = commandedSetpoint;
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(String key) {
        return (T) attributes.get(key);
    }

    public void putAttr(String key, Object value) {
        attributes.put(key, value);
    }
}
