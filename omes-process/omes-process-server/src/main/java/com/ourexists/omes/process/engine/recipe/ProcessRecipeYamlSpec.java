package com.ourexists.omes.process.engine.recipe;

import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessRecipeYamlSpec {

    private String name;
    private String description;
    /** 工序引擎模板（COMBINATION）；斜坡 segments 由工序 params 动态注入 */
    private ProcessStepDefinition engine;
    private String equipmentCode;
    private String variable = "temp";
    private ProcessRecipeYamlRamp ramp = new ProcessRecipeYamlRamp();
    private ProcessRecipeYamlShutdown shutdown = new ProcessRecipeYamlShutdown();

    @Data
    public static class ProcessRecipeYamlRamp {
        private List<ProcessRecipeYamlSegment> segments = new ArrayList<>();
    }

    @Data
    public static class ProcessRecipeYamlSegment {
        private Double to;
        private Object duration;
        private Object holdDuration;
    }

    @Data
    public static class ProcessRecipeYamlShutdown {
        private Boolean enabled = true;
        private String variable = "setpoint";
        private Double target = 0D;
    }
}
