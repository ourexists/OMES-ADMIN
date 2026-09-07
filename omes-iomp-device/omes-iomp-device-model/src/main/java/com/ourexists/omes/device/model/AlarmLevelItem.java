/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "报警等级项(枚举)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmLevelItem {

    @Schema(description = "等级编码，与 AlarmLevelEnum.code 一致")
    private Integer code;

    @Schema(description = "等级描述")
    private String desc;
}
