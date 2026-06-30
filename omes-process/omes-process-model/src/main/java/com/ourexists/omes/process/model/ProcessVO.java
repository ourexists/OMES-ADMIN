package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工艺详情")
public class ProcessVO extends ProcessListVO {

    @Schema(description = "压模图号列表")
    private List<ProcessMoldItem> molds = new ArrayList<>();

    @Schema(description = "工序列表")
    private List<ProcessStepItem> steps = new ArrayList<>();
}
