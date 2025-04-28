/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author pengcheng
 * @date 2022/4/12 18:54
 * @since 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_ucenter_tenant_permission")
public class TenantPermission {

    private String tenantId;

    private String permissionId;
}
