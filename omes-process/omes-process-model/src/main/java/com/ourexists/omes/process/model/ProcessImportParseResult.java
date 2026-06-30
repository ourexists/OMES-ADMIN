package com.ourexists.omes.process.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工艺卡片 Word/PDF 解析结果")
public class ProcessImportParseResult extends ProcessSaveRequest {

    @Schema(description = "解析提示信息")
    private List<String> warnings = new ArrayList<>();

    @Schema(description = "工艺简图完整访问地址")
    private String processImageAccessUrl;

    @JsonIgnore
    private byte[] processImageBytes;

    @JsonIgnore
    private String processImageExtension;

    @JsonIgnore
    private String processImageContentType;
}
