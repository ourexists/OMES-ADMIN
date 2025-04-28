/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.tenant.TenantPageQuery;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@RequestMapping("/overall/tenant")
public interface TenantOverallFeign {

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery);
}
