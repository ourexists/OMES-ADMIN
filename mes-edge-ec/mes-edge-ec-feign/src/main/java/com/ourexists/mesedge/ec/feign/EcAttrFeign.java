/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ec.model.EcAttrBatchDto;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/mat")
public interface EcAttrFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<EcAttrDto>> selectByPage(@RequestBody EcAttrPageQuery dto);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EcAttrDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EcAttrBatchDto dto);

}
