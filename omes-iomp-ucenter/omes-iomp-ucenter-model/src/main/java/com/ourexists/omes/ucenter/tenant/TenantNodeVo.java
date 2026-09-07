/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.ucenter.tenant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TenantNodeVo extends TenantVo {

    private TenantVo pTenant;
}
