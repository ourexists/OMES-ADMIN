/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopPageQuery;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/mat")
public interface WorkshopFeign {

    JsonResponseEntity<List<WorkshopTreeNode>> selectTree();

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody WorkshopDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

}
