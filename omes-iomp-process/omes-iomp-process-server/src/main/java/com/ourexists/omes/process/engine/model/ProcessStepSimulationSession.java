package com.ourexists.omes.process.engine.model;

import com.ourexists.omes.process.engine.ProcessSequenceRunner;
import com.ourexists.omes.process.engine.spi.InMemoryProcessSignalProvider;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
public class ProcessStepSimulationSession {

    private final String sessionId;
    private final String processId;
    private final ProcessStepScript script;
    private final InMemoryProcessSignalProvider signalProvider;
    private final ProcessSequenceRunner.RunningSequence sequence;
    private final Instant createdAt;

    @Setter
    private Instant lastTickAt;

    public ProcessStepSimulationSession(String sessionId,
                                        String processId,
                                        ProcessStepScript script,
                                        InMemoryProcessSignalProvider signalProvider,
                                        ProcessSequenceRunner.RunningSequence sequence) {
        this.sessionId = sessionId;
        this.processId = processId;
        this.script = script;
        this.signalProvider = signalProvider;
        this.sequence = sequence;
        this.createdAt = Instant.now();
    }
}
