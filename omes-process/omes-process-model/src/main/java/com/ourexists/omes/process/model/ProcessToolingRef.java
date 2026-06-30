package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工序关联工装")
public class ProcessToolingRef {

    @Schema(description = "工装编号")
    @NotBlank(message = "工装编号不能为空")
    private String toolingCode;

    @Schema(description = "工装名称")
    @NotBlank(message = "工装名称不能为空")
    private String toolingName;
}
