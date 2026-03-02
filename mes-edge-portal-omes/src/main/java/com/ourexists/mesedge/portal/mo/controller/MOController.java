/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mo.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.mat.feign.MATFeign;
import com.ourexists.mesedge.mat.model.MaterialDto;
import com.ourexists.mesedge.mo.enums.MOStatusEnum;
import com.ourexists.mesedge.mo.feign.MOFeign;
import com.ourexists.mesedge.mo.model.MODetailDto;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "生产订单")
@RestController
@RequestMapping("/mo")
public class MOController {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MATFeign matFeign;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MODto>> selectByPage(@RequestBody MOPageQuery dto) {
        return moFeign.selectByPage(dto);
    }

    @Operation(summary = "通过id查询所有", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<MODto> selectById(@RequestParam String id) {
        return moFeign.selectById(id);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MODto dto) {
        if (CollectionUtil.isNotBlank(dto.getDetailDtoList())) {
            dto.getDetailDtoList().forEach(e -> e.setId(null));
            List<String> matCodes = dto.getDetailDtoList().stream().map(MODetailDto::getMatCode).distinct().collect(Collectors.toList());
            IdsDto idsDto = new IdsDto();
            idsDto.setIds(matCodes);
            try {
                List<MaterialDto> mats = RemoteHandleUtils.getDataFormResponse(matFeign.selectByCodes(idsDto));
                List<MaterialDto> exists = new ArrayList<>();
                for (MaterialDto mat : mats) {
                    for (MODetailDto detailDto : dto.getDetailDtoList()) {
                        if (mat.getSelfCode().equals(detailDto.getMatCode())) {
                            detailDto.setMatId(mat.getId());
                            exists.add(mat);
                        }
                    }
                }
                mats.removeAll(exists);
                if (CollectionUtil.isNotBlank(mats)) {
                    StringBuilder error = new StringBuilder();
                    for (MaterialDto mat : mats) {
                        error.append(mat.getSelfCode()).append("-").append(mat.getName()).append("|");
                    }
                    throw new BusinessException("${mo.mat.nomatch}", error.substring(0, error.length() - 1));
                }
            } catch (EraCommonException e) {
                throw new RuntimeException(e);
            }
        }
        return moFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return moFeign.delete(idsDto);
    }


    @Operation(summary = "状态", description = "状态")
    @GetMapping("status")
    public JsonResponseEntity<List<MapDto>> status() {
        List<MapDto> r = new ArrayList<>();
        for (MOStatusEnum value : MOStatusEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }

}
