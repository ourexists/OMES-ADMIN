/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.feign.ReportFeign;
import com.ourexists.mesedge.report.model.MatCountQuery;
import com.ourexists.mesedge.report.model.MatCountVo;
import com.ourexists.mesedge.report.model.ProductionCountQuery;
import com.ourexists.mesedge.report.model.ProductionCountVo;
import com.ourexists.mesedge.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "任务管理")
//@RestController
//@RequestMapping("/task")
@Component
public class ReportViewer implements ReportFeign {

    @Autowired
    private ReportService reportService;

    @PostMapping("matCount")
    public JsonResponseEntity<List<MatCountVo>> matCount(@RequestBody MatCountQuery dto) {
        return JsonResponseEntity.success(reportService.matCount(dto));
    }

    @PostMapping("productionCount")
    public JsonResponseEntity<List<ProductionCountVo>> productionCount(@RequestBody ProductionCountQuery query) {
        return JsonResponseEntity.success(reportService.productionCount(query));
    }
}
