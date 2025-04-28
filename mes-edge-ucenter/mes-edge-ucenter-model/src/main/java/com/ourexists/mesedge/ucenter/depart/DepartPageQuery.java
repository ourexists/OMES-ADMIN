/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/11 15:27
 * @since 1.0.0
 */
@Getter
@Setter
public class DepartPageQuery extends PageQuery {

    @Schema(name = "用户组名")
    private String name;

    @Schema(name = "用户组编号")
    private String code;

    @Schema(name = "所属租户id")
    private String tenantId;

}
