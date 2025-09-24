/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.report;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.report.feign.LmRecordFeign;
import com.ourexists.mesedge.report.model.LmRecordDto;
import com.ourexists.mesedge.report.model.LmRecordPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lmrecord")
public class LmRecordController {

    @Autowired
    private LmRecordFeign lmRecordFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<LmRecordDto>> selectByPage(@RequestBody LmRecordPageQuery dto) {
        return lmRecordFeign.selectByPage(dto);
    }

}
