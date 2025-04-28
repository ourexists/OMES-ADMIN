/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

/**
 * @author pengcheng
 * @date 2022/4/11 16:55
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class TenantClientDetails extends BaseDto {

    private static final long serialVersionUID = -5865887803421501602L;
    @Schema(description = "客户端认证id")
    private String clientId;

    @Schema(description = "客户端访问密匙")
    private String clientSecret;

    @Schema(description = "客户端申请的权限范围", allowableValues = "read,write,trust")
    private Set<String> scope;

    @Schema(description = "资源")
    private Set<String> resourceIds;

    @Schema(description = "指定客户端支持的grant_type", allowableValues = "authorization_code,password,refresh_token")
    private Set<String> authorizedGrantTypes;

    @Schema(description = "客户端的access_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 12, 12小时)")
    private Integer accessTokenValiditySeconds;

    @Schema(description = "客户端的refresh_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 24 * 30, 30天)")
    private Integer refreshTokenValiditySeconds;

    private Map<String, Object> additionalInformation;
}
