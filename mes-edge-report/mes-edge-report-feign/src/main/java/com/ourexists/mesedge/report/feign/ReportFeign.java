package com.ourexists.mesedge.report.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.model.MatCountQuery;
import com.ourexists.mesedge.report.model.MatCountVo;
import com.ourexists.mesedge.report.model.ProductionCountQuery;
import com.ourexists.mesedge.report.model.ProductionCountVo;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ReportFeign {

    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("matCount")
    JsonResponseEntity<List<MatCountVo>> matCount(@RequestBody MatCountQuery dto);

    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("productionCount")
    JsonResponseEntity<List<ProductionCountVo>> productionCount(@RequestBody ProductionCountQuery query);
}
