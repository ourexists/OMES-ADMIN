/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.viewer;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.line.feign.TFFeign;
import com.ourexists.mesedge.line.model.ChangePriorityDto;
import com.ourexists.mesedge.line.model.TFDto;
import com.ourexists.mesedge.line.model.TFVo;
import com.ourexists.mesedge.line.pojo.TF;
import com.ourexists.mesedge.line.service.TFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "工艺流程")
//@RestController
//@RequestMapping("/tf")
@Component
public class TFViewer implements TFFeign {

    @Autowired
    private TFService service;

//    @Operation(summary = "根据产线id查询", description = "")
//    @GetMapping("selectByLineId")
    public JsonResponseEntity<List<TFVo>> selectByLineId(@RequestParam String lineId) {
        return JsonResponseEntity.success(TF.covert(service.selectByLineId(lineId)));
    }

//    @Operation(summary = "通过id查询", description = "")
//    @GetMapping("selectById")
    public JsonResponseEntity<TFVo> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(TF.covert(service.getById(id)));
    }

//    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TFDto dto) {
        service.saveOrUpdate(TF.wrap(dto));
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

//    @Operation(summary = "改变优先级", description = "改变优先级")
//    @PostMapping("changePriority")
    public JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto) {
        service.changePriority(dto);
        return JsonResponseEntity.success(true);
    }
}
