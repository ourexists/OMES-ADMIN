/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/7 18:07
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccPageQuery extends PageQuery {

    private static final long serialVersionUID = -5094206202434360288L;

    @Schema(description = "所属平台")
    private String platform;

    @Schema(description = "所属租户")
    private String tenantId;

    @Schema(description = "账户名")
    private String accName;

    @Schema(description = "账户昵称")
    private String nickName;

    @Schema(description = "账户状态")
    private Integer status;

    @Schema(description = "账户过期区间开始")
    private Date expireStartTime;

    @Schema(description = "账户过期区间结束")
    private Date expireEndTime;

    @Schema(description = "账户手机号")
    private String mobile;

    @Schema(description = "账户完善条件")
    private Integer perfection;

    @Schema(description = "账户id")
    private List<String> ids;

    @Schema(description = "是否查询租户信息")
    private Boolean queryTenant;

    @Schema(description = "账户角色")
    private String accRole;

    @Schema(description = "父租户")
    private String pTenant;

    @Schema(description = "查询子集")
    private Boolean queryChild = false;

    public Boolean getQueryTenant() {
        if (this.queryTenant == null) {
            return false;
        }
        return queryTenant;
    }
}
