package com.ourexists.omes.process.service;

import com.ourexists.omes.process.engine.ProcessSequenceRunner;
import com.ourexists.omes.process.engine.ProcessStepScriptCodec;
import com.ourexists.omes.process.engine.model.ProcessStepScript;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.spi.ProcessSignalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessStepExecutionService {

    private final ProcessStepScriptCodec scriptCodec;
    private final ProcessSequenceRunner sequenceRunner;

    public ProcessSequenceRunner.RunningSequence startScript(
            String stepScriptJson, ProcessSignalProvider signalProvider) {
        return startScript(scriptCodec.parseScript(stepScriptJson), signalProvider);
    }

    public ProcessSequenceRunner.RunningSequence startScript(
            ProcessStepScript script, ProcessSignalProvider signalProvider) {
        return sequenceRunner.start(script, signalProvider);
    }

    public ProcessStepTickResult tick(ProcessSequenceRunner.RunningSequence sequence, long deltaMs) {
        ProcessStepTickResult result = sequenceRunner.tick(sequence, deltaMs);
        sequence.record(result);
        return result;
    }
}
