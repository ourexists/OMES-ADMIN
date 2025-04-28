/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ucenter.feign.TenantOverallFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantPageQuery;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import com.ourexists.mesedge.ucenter.tenant.pojo.Tenant;
import com.ourexists.mesedge.ucenter.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@Slf4j
//@Tag(name = "租户")
//@RestController
//@RequestMapping("/overall/tenant")
@Component
public class TenantOverallViewer implements TenantOverallFeign {

    @Autowired
    private TenantService tenantService;

    @Operation(summary = "分页查询")
    @PostMapping("/selectByPage")
    public JsonResponseEntity<List<TenantVo>> selectByPage(@RequestBody TenantPageQuery tenantPageQuery) {
        Page<Tenant> page = tenantService.selectByPage(tenantPageQuery);
        return JsonResponseEntity.success(Tenant.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }
}
