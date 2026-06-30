package com.ourexists.omes.process.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工艺工序步骤脚本")
public class ProcessStepScript {

    @Schema(description = "步骤列表")
    private List<ProcessStepDefinition> steps = new ArrayList<>();
}
