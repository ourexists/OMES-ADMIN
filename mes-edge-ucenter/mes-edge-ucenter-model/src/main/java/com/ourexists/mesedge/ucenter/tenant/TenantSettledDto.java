/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class TenantSettledDto extends BaseDto {

    private static final long serialVersionUID = -5139652996423605914L;
    @Schema(description = "租户编号")
    @NotBlank(message = "租户编号不能为空！")
    private String tenantCode;

    @Schema(description = "租戶logo")
    private String logo;

    @Schema(description = "父租户编号")
    private String pcode;

    @Schema(description = "租戶名")
    @NotBlank(message = "租户名不能为空！")
    private String tenantName;

    @Schema(description = "租戶入驻时间")
    private Date settledTime;

    @Schema(description = "租户到期时间")
    private Date expireTime;

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

    @Schema(description = "管控权")
    private Integer management;

    @Schema(description = "管控数量")
    private Integer manageNum;

    private TenantClientDetails tenantClientDetails;

}
