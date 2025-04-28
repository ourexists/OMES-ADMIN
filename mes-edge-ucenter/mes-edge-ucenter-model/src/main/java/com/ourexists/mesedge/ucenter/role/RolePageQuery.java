/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/12 16:03
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class RolePageQuery extends PageQuery {

    @Schema(description ="角色名")
    private String name;

    @Schema(description ="角色编号")
    private String code;

    @Schema(description ="角色类别")
    private Integer type = 0;

    private String tenantId;

    @Schema(description ="id")
    private List<String> ids;
}
