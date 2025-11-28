/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.model.vo.BaseVo;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.ucenter.permission.PermissionApiDetailDto;
import com.ourexists.mesedge.ucenter.tenant.TenantUVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pengcheng
 * @date 2022/4/6 18:04
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccVo extends BaseVo {

    private static final long serialVersionUID = 7450812109919833697L;
    private String id;

    @Schema(description = "所属租户信息（只用于视图展示）")
    private List<TenantUVo> tenantVos;

    @Schema(description = "账户所有租户角色")
    private Map<String, String> tenantRoles;

    @Schema(description = "账户所有api权限")
    private List<PermissionApiDetailDto> permissionApiDetailDtos;

    @Schema(description = "账户名")
    private String accName;

    @Schema(description = "账户密码")
    private String password;

    @Schema(description = "账户昵称")
    private String nickName;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "账户入驻时间")
    private Date settledTime;

    @Schema(description = "账户过期时间")
    private Date expireTime;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "出生年月")
    private Date birthDay;

    @Schema(description = "用户性别")
    private Integer sex;

    @Schema(description = "账户状态")
    private Integer status;

    @Schema(description = "账户状态描述")
    private String statusDesc;

    @Schema(description = "账户手机号")
    private String mobile;

    @Schema(description = "账户邮箱")
    private String email;

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

    @Schema(description = "平台")
    private String platform;

    private String source;

    private String sourceId;

    private String unionId;

    private String avatarUrl;

    private String country;

    private String province;

    private String city;

    private String language;

    private Integer perfection;

    private String detail;

    public Map<String, String> getTenantRoles() {
        if (CollectionUtil.isNotBlank(this.tenantVos)) {
            Map<String, String> r = new HashMap<>(this.tenantVos.size());
            for (TenantUVo tenantVo : this.tenantVos) {
                r.put(tenantVo.getTenantCode(), tenantVo.getRole());
            }
            return r;
        }
        return null;
    }
}
