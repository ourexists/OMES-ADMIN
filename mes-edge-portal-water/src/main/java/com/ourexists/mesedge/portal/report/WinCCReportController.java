package com.ourexists.mesedge.portal.report;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/winCCReport")
public class WinCCReportController {

    @Autowired
    private WinCCReportFeign winCCReportFeign;

    @Operation(summary = "分页查询数据报表", description = "分页查询数据报表")
    @PostMapping("selectDataListByPage")
    public JsonResponseEntity<WinCCDatalistResDto> selectDataListByPage(@RequestBody WinCCDatalistPageQuery dto) throws IllegalAccessException {
        return winCCReportFeign.selectDataListByPage(dto);
    }

    @Operation(summary = "分页查询加药设备数据", description = "分页查询加药设备数据")
    @PostMapping("selectDosingByPage")
    public JsonResponseEntity<List<WinCCDosingDevDto>> selectDosingByPage(@RequestBody WinCCDosingPageQuery dto) {
        return winCCReportFeign.selectDosingByPage(dto);
    }

    @Operation(summary = "分页查询磁混凝设备数据", description = "分页查询磁混凝设备数据")
    @PostMapping("selectMagByPage")
    public JsonResponseEntity<List<WinCCMagDevDto>> selectMagByPage(@RequestBody WinCCMagPageQuery dto) {
        return winCCReportFeign.selectMagByPage(dto);
    }

    @Operation(summary = "分页查询1期氧化沟1#设备数据", description = "分页查询1期氧化沟1#设备数据")
    @PostMapping("selectOd11ByPage")
    public JsonResponseEntity<List<WinCCOd11DevDto>> selectOd11ByPage(@RequestBody WinCCOd11PageQuery dto) {
        return winCCReportFeign.selectOd11ByPage(dto);
    }

    @Operation(summary = "分页查询1期氧化沟2#设备数据", description = "分页查询1期氧化沟2#设备数据")
    @PostMapping("selectOd12ByPage")
    public JsonResponseEntity<List<WinCCOd12DevDto>> selectOd12ByPage(@RequestBody WinCCOd12PageQuery dto) {
        return winCCReportFeign.selectOd12ByPage(dto);
    }

    @Operation(summary = "分页查询2期氧化沟设备数据", description = "分页查询2期氧化沟设备数据")
    @PostMapping("selectOd20ByPage")
    public JsonResponseEntity<List<WinCCOd20DevDto>> selectOd20ByPage(@RequestBody WinCCOd20PageQuery dto) {
        return winCCReportFeign.selectOd20ByPage(dto);
    }

    @Operation(summary = "分页查询提升泵房设备数据", description = "分页查询提升泵房设备数据")
    @PostMapping("selectWpsByPage")
    public JsonResponseEntity<List<WinCCWpsDevDto>> selectWpsByPage(@RequestBody WinCCWpsPageQuery dto) {
        return winCCReportFeign.selectWpsByPage(dto);
    }
}
