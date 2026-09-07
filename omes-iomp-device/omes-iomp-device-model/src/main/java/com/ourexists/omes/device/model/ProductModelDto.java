package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 产品型号：挂在产品下，维护该型号的属性映射。
 */
@Schema(description = "产品型号")
@Getter
@Setter
@Accessors(chain = true)
public class ProductModelDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "所属产品编号")
    private String productCode;

    @Schema(description = "型号名称")
    private String name;

    @Schema(description = "型号编号")
    private String code;

    @Schema(description = "属性映射（runMap/attrs/alarms/controls 的 map）")
    private EquipConfigDetail attrConfig;
}
