/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/3/27 15:21
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class TenantTreeNode extends TreeNode<TenantTreeNode> {

    @Schema(description = "id")
    private String id;

    @Schema(description = "租戶名")
    private String tenantName;

    @Schema(description = "租户状态")
    private Integer status;

    @Schema(description = "租户状态描述")
    private String statusDesc;

    @Schema(description = "租戶入驻时间")
    private Date settledTime;

    @Schema(description = "租户到期时间")
    private Date expireTime;

    @Schema(description = "创建人")
    protected String createdBy;

    @Schema(description = "创建人id")
    protected String createdId;

    @Schema(description = "创建时间")
    protected Date createdTime;

    @Schema(description = "更新人")
    protected String updatedBy;

    @Schema(description = "更新人id")
    protected String updatedId;

    @Schema(description = "更新时间")
    protected Date updatedTime;

    @Schema(description = "租户/机构所在地址")
    private String tenantAddress;

    @Schema(description = "联系人姓名")
    private String tenantContacts;

    @Schema(description = "服务电话")
    private String tenantPhone;

    @Schema(description = "客服电话")
    private String tel;

    @Schema(description = "联系邮箱")
    private String tenantMail;

    @Schema(description = "省份code")
    private String provinceCode;

    @Schema(description = "城市code")
    private String cityCode;

    @Schema(description = "区县code")
    private String countyCode;

    @Schema(description = "街道code")
    private String streetCode;

    @Schema(description = "所在区域")
    private String areaFullname;

    @Schema(description = "租户机构经纬度坐标")
    private String tenantCoo;

    @Schema(description = "用户与租户机构距离（单位km，保留两位小数）")
    private Double distance;

    @Schema(description = "管控权")
    private Integer management;

    @Schema(description = "管控数量")
    private Integer manageNum;
}
