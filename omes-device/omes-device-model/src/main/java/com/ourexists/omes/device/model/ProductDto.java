/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 产品（原设备类型独立为产品管理）
 */
@Schema(description = "产品")
@Getter
@Setter
@Accessors(chain = true)
public class ProductDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品编号")
    private String code;

    @Schema(description = "产品图片地址")
    private String imageUrl;
}
