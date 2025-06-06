/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.flow;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSFlowDto;
import com.ourexists.mesedge.portal.mps.model.MPSVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "生产计划")
@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private MpsFlowManager mpsFlowManager;

    @Operation(summary = "计划流程", description = "计划流程")
    @PostMapping("mps")
    public JsonResponseEntity<List<MPSVo>> flow(@RequestBody MPSFlowDto flowDto) {
        return JsonResponseEntity.success(mpsFlowManager.flowCalc(flowDto));
    }


    @Operation(summary = "计划流程完成", description = "计划完成")
    @PostMapping("mpsFlowComplete")
    public JsonResponseEntity<Boolean> mpsFlowComplete(@RequestBody List<MPSDto> dtos) {
        mpsFlowManager.mpsFlowComplete(dtos);
        return JsonResponseEntity.success(true);
    }
}
