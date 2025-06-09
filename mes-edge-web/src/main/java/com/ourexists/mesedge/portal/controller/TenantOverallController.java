/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantPageQuery;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "租户")
@RestController
@RequestMapping("/overall/tenant")
public class TenantOverallController {

    @Autowired
    private TenantFeign tenantFeign;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery) {
        return tenantFeign.selectByPage(tenantPageQuery);
    }
}
