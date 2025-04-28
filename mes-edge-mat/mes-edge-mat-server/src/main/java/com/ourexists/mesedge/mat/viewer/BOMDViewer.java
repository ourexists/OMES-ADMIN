/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.viewer;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.feign.BOMDFeign;
import com.ourexists.mesedge.mat.model.BOMDDto;
import com.ourexists.mesedge.mat.pojo.BOMD;
import com.ourexists.mesedge.mat.service.BOMDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

//@Tag(name = "配方详情")
//@RestController
//@RequestMapping("/BOMD")
@Component
public class BOMDViewer implements BOMDFeign {

    @Autowired
    private BOMDService service;

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMDDto dto) {
        service.saveOrUpdate(BOMD.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
