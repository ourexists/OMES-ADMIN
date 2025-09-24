/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.report;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.feign.ReportFeign;
import com.ourexists.mesedge.report.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "生产订单")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportFeign reportFeign;

    @PostMapping("matCount")
    public JsonResponseEntity<List<MatCountVo>> matCount(@RequestBody MatCountQuery dto) {
        return reportFeign.matCount(dto);
    }

    @PostMapping("productionCount")
    public JsonResponseEntity<List<ProductionCountVo>> productionCount(@RequestBody ProductionCountQuery query) {
        return reportFeign.productionCount(query);
    }
}
