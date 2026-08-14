package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品属性模板（对齐 113 设备类型变量定义）。
 * 不含采集地址；各设备按 name 映射 map。
 */
@Schema(description = "产品属性模板")
@Getter
@Setter
@Accessors(chain = true)
public class ProductAttrConfig {

    @Schema(description = "属性定义")
    private List<EquipAttr> attrs = new ArrayList<>();

    @Schema(description = "报警定义")
    private List<EquipAlarm> alarms = new ArrayList<>();

    @Schema(description = "控制定义")
    private List<EquipControl> controls = new ArrayList<>();
}
