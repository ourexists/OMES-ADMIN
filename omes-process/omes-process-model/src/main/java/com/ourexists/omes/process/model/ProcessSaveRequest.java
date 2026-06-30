package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "工艺保存请求（创建/更新共用字段）")
public class ProcessSaveRequest {

    @Schema(description = "工艺ID（更新时必填）")
    private String id;

    @Schema(description = "操作对象名称")
    private String name;

    @Schema(description = "工艺编号")
    @NotBlank(message = "工艺编号不能为空")
    @Size(max = 64, message = "工艺编号长度不能超过64")
    private String processCode;

    @Schema(description = "工艺简图文件相对存储路径")
    private String processImageUrl;

    @Schema(description = "工艺名称")
    @NotBlank(message = "工艺名称不能为空")
    @Size(max = 128, message = "工艺名称长度不能超过128")
    private String processName;

    @Schema(description = "关联产品编号")
    private String productCode;

    @Schema(description = "关联产品名称")
    private String productName;

    @Schema(description = "关联零组件编号")
    private String componentCode;

    @Schema(description = "关联零组件名称")
    private String componentName;

    @Schema(description = "关联材料编号")
    private String materialCode;

    @Schema(description = "关联材料名称")
    private String materialName;

    @Schema(description = "技术条件编号")
    @Size(max = 64, message = "技术条件编号长度不能超过64")
    private String techCondition;

    @Schema(description = "材料预热")
    private String materialPreheat;

    @Schema(description = "压机压力")
    private BigDecimal pressPressure;

    @Schema(description = "毛料重量")
    private BigDecimal blankWeight;

    @Schema(description = "毛料重量上偏移")
    private BigDecimal blankWeightUpperOffset;

    @Schema(description = "毛料重量下偏移")
    private BigDecimal blankWeightLowerOffset;

    @Schema(description = "压制温度")
    private BigDecimal pressTemperature;

    @Schema(description = "压制温度上偏移")
    private BigDecimal pressTemperatureUpperOffset;

    @Schema(description = "压制温度下偏移")
    private BigDecimal pressTemperatureLowerOffset;

    @Schema(description = "保持时间（秒）")
    private Integer holdTimeSeconds;

    @Schema(description = "压模图号列表")
    @Valid
    private List<ProcessMoldItem> molds = new ArrayList<>();

    @Schema(description = "工序列表")
    @Valid
    private List<ProcessStepItem> steps = new ArrayList<>();
}
