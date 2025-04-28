/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.view;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.feign.MPSDetailFeign;
import com.ourexists.mesedge.mps.model.MPSDetailDto;
import com.ourexists.mesedge.mps.pojo.MPSDetail;
import com.ourexists.mesedge.mps.service.MPSDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

//@Tag(name = "成分")
//@RestController
//@RequestMapping("/mps_detail")
@Component
public class MPSDetailViewer implements MPSDetailFeign {

    @Autowired
    private MPSDetailService service;

//    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDetailDto dto) {
        service.saveOrUpdate(MPSDetail.wrap(dto));
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
