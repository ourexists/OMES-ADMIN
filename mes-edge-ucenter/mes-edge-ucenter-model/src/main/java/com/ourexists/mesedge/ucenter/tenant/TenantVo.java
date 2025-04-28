/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
@Accessors(chain = true)
public class TenantVo extends BaseVo implements Comparable<TenantVo> {

    private static final long serialVersionUID = -2113660062787354441L;
    @Schema(description = "id")
    private String id;

    @Schema(description = "租戶名")
    private String tenantName;

    @Schema(description = "租户编号")
    private String tenantCode;

    @Schema(description = "租戶logo")
    private String logo;

    @Schema(description = "父租户编号")
    private String pcode;

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

    @Override
    public int compareTo(TenantVo tenantVo) {
        //降序：返回值为1 或-1 升序改变变量位置即可
        return this.getDistance() - tenantVo.getDistance() > 0 ? 1 : ((this.getDistance().doubleValue() == tenantVo.getDistance().doubleValue()) ? 0 : -1);
    }
}
