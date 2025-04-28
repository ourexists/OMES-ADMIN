/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/6 17:31
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@Schema
public class TenantPageQuery extends PageQuery {

    private static final long serialVersionUID = 158844596327474951L;

    @Schema(description = "租戶名")
    private String tenantName;

    @Schema(description = "租戶编号")
    private String tenantCode;

    @Schema(description = "租戶编号s")
    private List<String> tenantCodes;

    @Schema(description = "归属租户")
    private String pcode;

    @Schema(description = "租戶状态")
    private Integer status;

    @Schema(description = "租戶入驻区间开始")
    private Date settledStartTime;

    @Schema(description = "租戶入驻区间结束")
    private Date settledEndTime;

    @Schema(description = "租户到期区间开始")
    private Date expireStartTime;

    @Schema(description = "租户到期区间结束")
    private Date expireEndTime;

    @Schema(description = "租户创建区间开始")
    private Date createdStartTime;

    @Schema(description = "租户创建区间结束")
    private Date createdEndTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "根据机构id批量查询")
    private List<String> tenantIds;

    @Schema(description = "管控权")
    private Integer management;
}
