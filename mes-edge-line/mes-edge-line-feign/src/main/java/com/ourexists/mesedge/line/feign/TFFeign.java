/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.line.model.ChangePriorityDto;
import com.ourexists.mesedge.line.model.TFDto;
import com.ourexists.mesedge.line.model.TFVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "工艺流程")
//@RestController
//@RequestMapping("/tf")
public interface TFFeign {

    //    @Operation(summary = "根据产线id查询", description = "")
//    @GetMapping("selectByLineId")
    JsonResponseEntity<List<TFVo>> selectByLineId(@RequestParam String lineId);

    //    @Operation(summary = "通过id查询", description = "")
//    @GetMapping("selectById")
    JsonResponseEntity<TFVo> selectById(@RequestParam String id);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TFDto dto);


    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    //    @Operation(summary = "改变优先级", description = "改变优先级")
//    @PostMapping("changePriority")
    JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto);

}
