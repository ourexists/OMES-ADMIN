package com.ourexists.mesedge.portal.report;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.portal.config.CacheUtils;
import com.ourexists.mesedge.report.feign.WinCCReportFeign;
import com.ourexists.mesedge.report.model.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.ourexists.mesedge.portal.task.WinCCDevConstants.CACHE_LIST;

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

    @Autowired
    private CacheUtils cacheUtils;

    @Operation(summary = "实时设备运行", description = "实时设备运行")
    @GetMapping("realtimeDevRun")
    public JsonResponseEntity<Map<String, Map<Object, Object>>> realtimeDevRun() {
        Map<String, Map<Object, Object>> r = new HashMap<>();
        for (String cacheName : CACHE_LIST) {
            Cache<Object, Object> runCache = cacheUtils.nativeCache(cacheName);
            ConcurrentMap<Object, Object> runCacheMap = runCache.asMap();
            r.put(cacheName, runCacheMap);
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "实时设备报警", description = "实时设备报警")
    @GetMapping("realtimeDevAlarm")
    public JsonResponseEntity<Map<String, Map<Object, Object>>> realtimeDevAlarm() {
        Map<String, Map<Object, Object>> r = new HashMap<>();
        for (String cacheName : CACHE_LIST) {
            Cache<Object, Object> runCache = cacheUtils.nativeCache(cacheName + "_alarm");
            ConcurrentMap<Object, Object> runCacheMap = runCache.asMap();
            r.put(cacheName, runCacheMap);
        }
        return JsonResponseEntity.success(r);
    }
}
