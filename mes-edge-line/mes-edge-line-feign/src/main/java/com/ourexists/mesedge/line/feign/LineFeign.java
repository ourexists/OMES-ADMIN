/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.line.model.LineDto;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.line.model.query.LinePageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */

public interface LineFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<LineVo>> selectByPage(@RequestBody LinePageQuery dto);

    //    @Operation(summary = "通过code查询所有", description = "")
//    @GetMapping("selectByCode")
    JsonResponseEntity<LineVo> selectByCode(@RequestParam String code);

    //    @Operation(summary = "通过id查询所有", description = "")
//    @GetMapping("selectById")
    JsonResponseEntity<LineVo> selectById(@RequestParam String id);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody LineDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    //    @Operation(summary = "重置工艺", description = "")
//    @PostMapping("resetLineTF")
    JsonResponseEntity<Boolean> resetLineTF(@RequestBody ResetLineTFDto dto);

    JsonResponseEntity<Boolean> updatePushTime(@RequestParam String lineId);

    JsonResponseEntity<List<LineVo>> selectByCodes(@Validated @RequestBody List<String> codes);
}
