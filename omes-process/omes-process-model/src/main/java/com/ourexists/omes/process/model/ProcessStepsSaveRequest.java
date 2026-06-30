package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工艺工序批量保存请求（替换该工艺下全部工序）")
public class ProcessStepsSaveRequest {

    @Schema(description = "工艺 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工艺ID不能为空")
    private String processId;

    @Schema(description = "工序列表（按顺序；可为空列表表示清空）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工序列表不能为null")
    @Valid
    private List<ProcessStepItem> steps = new ArrayList<>();
}
