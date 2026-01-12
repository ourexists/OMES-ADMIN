/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.message.model.NotifyVo;
import com.ourexists.mesedge.message.model.query.NotifyPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "通知")
//@RestController
//@RequestMapping("/notify")
public interface NotifyFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<NotifyVo>> selectByPage(@RequestBody NotifyPageQuery dto);


    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody NotifyDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);


    JsonResponseEntity<Boolean> updateStatus(@RequestParam String id, @RequestParam Integer status);


    JsonResponseEntity<Boolean> createAndStart(@Validated @RequestBody NotifyDto dto);
}
