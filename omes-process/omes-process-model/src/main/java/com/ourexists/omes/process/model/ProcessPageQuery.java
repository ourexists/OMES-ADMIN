package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工艺分页查询")
public class ProcessPageQuery {

    @Schema(description = "页码", example = "1")
    private int page = 1;

    @Schema(description = "每页条数", example = "10")
    private int size = 10;

    @Schema(description = "关键字（编号/名称）")
    private String keyword;
}
