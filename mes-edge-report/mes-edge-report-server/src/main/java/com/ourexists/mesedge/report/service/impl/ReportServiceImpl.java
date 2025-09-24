/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.ourexists.mesedge.report.mapper.ReportMapper;
import com.ourexists.mesedge.report.model.MatCountQuery;
import com.ourexists.mesedge.report.model.MatCountVo;
import com.ourexists.mesedge.report.model.ProductionCountQuery;
import com.ourexists.mesedge.report.model.ProductionCountVo;
import com.ourexists.mesedge.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<MatCountVo> matCount(MatCountQuery dto) {
        return reportMapper.selectMatCount(dto);
    }

    @Override
    public List<ProductionCountVo> productionCount(ProductionCountQuery query) {
        return reportMapper.selectPFCount(query);
    }
}
