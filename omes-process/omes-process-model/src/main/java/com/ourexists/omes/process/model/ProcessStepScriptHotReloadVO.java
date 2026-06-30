package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工序脚本热刷新结果")
public class ProcessStepScriptHotReloadVO {

    @Schema(description = "脚本链分组键")
    private String scriptChainKey;

    @Schema(description = "工序 ID（请求传入时回显）")
    private String stepId;

    @Schema(description = "驱动段数量")
    private int segmentCount;

    @Schema(description = "各段链信息")
    private List<ProcessStepScriptHotReloadSegmentVO> segments = new ArrayList<>();
}
