/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "生产订单")
//@RestController
//@RequestMapping("/mo")
public interface MOFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<MODto>> selectByPage(@RequestBody MOPageQuery dto);

    //    @Operation(summary = "通过id查询所有", description = "")
//    @GetMapping("selectById")
    JsonResponseEntity<MODto> selectById(@RequestParam String id);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MODto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<MODto> selectByCode(@RequestParam String moCode);

    JsonResponseEntity<Boolean> updateSurplus(@RequestParam String selfCode, @RequestParam Integer surplus);

    JsonResponseEntity<List<MODto>> selectByCodes(@RequestBody List<String> codes);

    JsonResponseEntity<Boolean> addBatch(@RequestBody List<MODto> dtos);
}
