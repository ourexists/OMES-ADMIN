/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.viewer;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ec.feign.EcRecordFeign;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.ec.model.EcRecordQuery;
import com.ourexists.mesedge.ec.pojo.EcRecord;
import com.ourexists.mesedge.ec.service.EcRecordService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class EcRecordViewer implements EcRecordFeign {

    @Autowired
    private EcRecordService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByCondition")
    public JsonResponseEntity<List<EcRecordDto>> selectByCondition(@RequestBody EcRecordQuery dto) {
        List<EcRecord> r = service.selectByCondition(dto);
        return JsonResponseEntity.success(EcRecord.covert(r));
    }


    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<List<EcRecordDto>> dto) {
        service.addBatch(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
