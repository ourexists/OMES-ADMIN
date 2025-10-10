package com.ourexists.mesedge.portal.report;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.WinCCDatalistDto;
import com.ourexists.mesedge.report.model.WinCCDatalistPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/winCCReport")
public class WinCCReportController {

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<WinCCDatalistDto>> selectByPage(WinCCDatalistPageQuery dto) {
        return winCCReportFeign.selectByPage(dto);
    }
}
