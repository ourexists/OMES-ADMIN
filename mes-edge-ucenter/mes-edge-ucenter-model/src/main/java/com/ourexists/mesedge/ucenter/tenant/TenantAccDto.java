/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/3/23 9:50
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class TenantAccDto extends BaseDto {

    private static final long serialVersionUID = -2382227839574096911L;

    private String tenantId;

    private String accId;

    private String role;
}
