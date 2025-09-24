/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service;

import com.ourexists.mesedge.report.model.MatCountQuery;
import com.ourexists.mesedge.report.model.MatCountVo;
import com.ourexists.mesedge.report.model.ProductionCountQuery;
import com.ourexists.mesedge.report.model.ProductionCountVo;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface ReportService {

    List<MatCountVo> matCount(MatCountQuery dto);

    List<ProductionCountVo> productionCount(ProductionCountQuery query);
}
