/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.expose;


import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSTFVo;
import com.ourexists.mesedge.portal.expose.model.MPSTFQuery;
import com.ourexists.mesedge.portal.line.service.LineFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "对外暴露接口")
@RestController
@RequestMapping("/expose")
public class ExposeController {

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineFlowService lineFlowService;

    @Operation(summary = "查询计划执行流程", description = "查询计划流程")
    @PostMapping("queryMPSFlow")
    public JsonResponseEntity<List<MPSTFVo>> queryMPSFlow(@Validated @RequestBody MPSTFQuery dto) {
        try {
            MPSDto mpsDto = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByCode(dto.getMpsCode()));
            if (mpsDto == null) {
                return JsonResponseEntity.success();
            }
            return JsonResponseEntity.success(mpsDto.getTfs());
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "重设流程计划", description = "重设流程计划")
    @PostMapping("resetLineTF")
    public JsonResponseEntity<Boolean> resetLineTF(@Validated @RequestBody ResetLineTFDto dto) {
        lineFlowService.resetLineTF(dto);
        return JsonResponseEntity.success(true);
    }
}
