/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.report;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "过程报表")
@RestController
@RequestMapping("/fzdata")
public class FzDataController {

    @Autowired
    private FzDataFeign fzDataFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<FzDataDto>> selectByPage(@RequestBody FzDataPageQuery dto) {
        return fzDataFeign.selectByPage(dto);
    }

    @Operation(summary = "所有的配方名", description = "所有的配方名")
    @GetMapping("allPFName")
    public JsonResponseEntity<List<String>> allPFName() {
        return fzDataFeign.allPFName();
    }

}
