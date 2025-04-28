/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import com.ourexists.mesedge.ucenter.tenant.TenantAccDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/3/23 9:55
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
@Accessors(chain = true)
public class AccBathSettleDto extends AccAddDto {

    private static final long serialVersionUID = 200001L;

    /**
     * 是否重置原有参数
     */
    @Schema(description ="是否重置原有租户信息")
    private Boolean isReset = true;

    /**
     * 租户角色
     */
    @Schema(description ="租户角色")
    private List<TenantAccDto> tenantAccDtoList;
}
