/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.model.MPSDetailDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

//@Tag(name = "成分")
//@RestController
//@RequestMapping("/mps_detail")
public interface MPSDetailFeign {

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDetailDto dto);

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
