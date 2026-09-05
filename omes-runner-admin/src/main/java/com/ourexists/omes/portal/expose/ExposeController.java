/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.expose;


import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.line.model.ResetLineTFDto;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.MPSDto;
import com.ourexists.omes.mps.model.MPSTFVo;
import com.ourexists.omes.portal.expose.model.MPSTFQuery;
import com.ourexists.omes.portal.line.service.LineFlowService;
import com.ourexists.omes.ucenter.feign.SystemConfigFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "对外暴露接口")
@RestController
@RequestMapping("/expose")
public class ExposeController {

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineFlowService lineFlowService;

    @Autowired
    private SystemConfigFeign systemConfigFeign;

    @Operation(summary = "前端公开配置", description = "供网关/前端启动时读取（含百度地图 AK）")
    @GetMapping("frontendConfig")
    public JsonResponseEntity<Map<String, Object>> frontendConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String ak = RemoteHandleUtils.getDataFormResponse(systemConfigFeign.getBaiduMapAk());
            if (StringUtils.hasText(ak)) {
                data.put("baiduMapAk", ak.trim());
            }
        } catch (EraCommonException e) {
            // 配置缺失时不阻断启动
        }
        return JsonResponseEntity.success(data);
    }

    @Operation(summary = "查询计划执行流程", description = "查询计划流程")
    @PostMapping("queryMPSFlow")
    public JsonResponseEntity<List<MPSTFVo>> queryMPSFlow(@Validated @RequestBody MPSTFQuery dto) {
        try {
            MPSDto mpsDto = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByCode(dto.getMpsCode()));
            if (mpsDto == null) {
                return JsonResponseEntity.success(new ArrayList<>());
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
