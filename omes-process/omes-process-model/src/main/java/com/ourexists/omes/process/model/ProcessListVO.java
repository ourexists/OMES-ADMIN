package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "工艺列表项")
public class ProcessListVO {

    @Schema(description = "工艺ID")
    private String id;

    @Schema(description = "工艺编号")
    private String processCode;

    @Schema(description = "工艺名称")
    private String processName;

    @Schema(description = "工艺简图文件相对存储路径")
    private String processImageUrl;

    @Schema(description = "工艺简图完整访问地址，仅查询响应")
    private String processImageAccessUrl;

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

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
