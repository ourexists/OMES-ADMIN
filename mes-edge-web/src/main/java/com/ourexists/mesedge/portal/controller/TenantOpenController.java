/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.controller;

import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantPageQuery;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "租户")
@RestController
@RequestMapping("/open/tenant")
public class TenantOpenController {

    @Autowired
    private TenantFeign tenantFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/openSelectByPage")
    public JsonResponseEntity<List<TenantVo>> openSelectByPage(@RequestBody TenantPageQuery tenantPageQuery) {
        return tenantFeign.selectByPage(tenantPageQuery);
    }

    @GetMapping("/all")
    @Operation(summary = "所有租户信息")
    public JsonResponseEntity<List<TenantVo>> all() {
        UserContext.setTenant(new TenantInfo().setTenantId(CommonConstant.SYSTEM_TENANT));
        return tenantFeign.all();
    }
}
