/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ec.model.EcRecordDto;
import com.ourexists.mesedge.ec.model.EcRecordQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/mat")
public interface EcRecordFeign {


    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByCondition")
    JsonResponseEntity<List<EcRecordDto>> selectByCondition(@RequestBody EcRecordQuery dto);


    //    @Operation(summary = "批量新增", description = "批量新增")
//    @PostMapping("addBatch")
    JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<List<EcRecordDto>> dto);

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

}
