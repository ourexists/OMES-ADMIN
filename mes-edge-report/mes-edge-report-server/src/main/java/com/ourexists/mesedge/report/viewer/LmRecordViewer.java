/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.report.feign.LmRecordFeign;
import com.ourexists.mesedge.report.model.LmRecord;
import com.ourexists.mesedge.report.model.LmRecordDto;
import com.ourexists.mesedge.report.model.LmRecordPageQuery;
import com.ourexists.mesedge.report.service.LmRecordService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "任务管理")
//@RestController
//@RequestMapping("/task")
@Component
public class LmRecordViewer implements LmRecordFeign {

    @Autowired
    private LmRecordService lmRecordService;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<LmRecordDto>> selectByPage(@RequestBody LmRecordPageQuery dto) {
        Page<LmRecord> page = lmRecordService.selectByPage(dto);
        return JsonResponseEntity.success(LmRecord.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

}
