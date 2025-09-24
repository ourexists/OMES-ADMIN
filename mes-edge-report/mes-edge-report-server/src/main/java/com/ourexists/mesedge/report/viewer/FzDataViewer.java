/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.report.feign.FzDataFeign;
import com.ourexists.mesedge.report.model.FzData;
import com.ourexists.mesedge.report.model.FzDataDto;
import com.ourexists.mesedge.report.model.FzDataPageQuery;
import com.ourexists.mesedge.report.service.FzDataService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "任务管理")
//@RestController
//@RequestMapping("/task")
@Component
public class FzDataViewer implements FzDataFeign {

    @Autowired
    private FzDataService fzDataService;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<FzDataDto>> selectByPage(@RequestBody FzDataPageQuery dto) {
        Page<FzData> page = fzDataService.selectByPage(dto);
        return JsonResponseEntity.success(FzData.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "所有配方名", description = "所有配方名")
    @GetMapping("allPFName")
    public JsonResponseEntity<List<String>> allPFName() {
        return JsonResponseEntity.success(fzDataService.allPFName());
    }

}
