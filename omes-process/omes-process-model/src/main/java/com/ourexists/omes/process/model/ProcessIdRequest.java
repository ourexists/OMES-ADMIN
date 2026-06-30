package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工艺ID请求")
public class ProcessIdRequest {

    @Schema(description = "工艺ID")
    @NotBlank(message = "工艺ID不能为空")
    private String id;

    @Schema(description = "操作对象名称")
    private String name;
}
