package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "压模图号项")
public class ProcessMoldItem {

    @Schema(description = "压模图号")
    @NotBlank(message = "压模图号不能为空")
    private String moldDrawingNo;

    @Schema(description = "压模槽数")
    @NotNull(message = "压模槽数不能为空")
    private Integer slotCount;
}
