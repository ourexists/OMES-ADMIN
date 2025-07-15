/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mat.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.feign.DeviceFeign;
import com.ourexists.mesedge.device.service.DeviceService;
import com.ourexists.mesedge.mat.feign.MATFeign;
import com.ourexists.mesedge.mat.model.MaterialDto;
import com.ourexists.mesedge.mat.model.query.MaterialPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.rmi.Remote;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "物料")
@RestController
@RequestMapping("/mat")
public class MATController {

    @Autowired
    private MATFeign matFeign;

    @Autowired
    private DeviceFeign deviceFeign;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MaterialDto>> selectByPage(@RequestBody MaterialPageQuery dto) {
        return matFeign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MaterialDto materialDto) {
        return matFeign.addOrUpdate(materialDto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        try {
            List<MaterialDto> materialDtos = RemoteHandleUtils.getDataFormResponse(matFeign.selectByIds(idsDto));
            if (CollectionUtil.isBlank(materialDtos)) {
                return JsonResponseEntity.success(true);
            }
            List<String> matCodes = materialDtos.stream().map(MaterialDto::getSelfCode).collect(Collectors.toList());
            Boolean isUsed = RemoteHandleUtils.getDataFormResponse(deviceFeign.isUseMat(matCodes));
            if (isUsed) {
                throw new BusinessException("${common.msg.date.use}");
            }
            return matFeign.delete(idsDto);
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

}
