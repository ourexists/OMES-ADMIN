/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.EquipAttrBatchDto;
import com.ourexists.mesedge.device.model.EquipAttrDto;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/mat")
public interface EquipAttrFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<EquipAttrDto>> selectByPage(@RequestBody EquipAttrPageQuery dto);

    JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EquipAttrBatchDto dto);

}
