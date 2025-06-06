/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mps.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.model.vo.Pagination;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.mo.feign.MOFeign;
import com.ourexists.mesedge.mo.model.MODto;
import com.ourexists.mesedge.mo.model.query.MOPageQuery;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.mps.model.ChangePriorityDto;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSQueueOperateDto;
import com.ourexists.mesedge.portal.mps.model.MPSViewPageQuery;
import com.ourexists.mesedge.portal.mps.model.MPSVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "生产计划")
@RestController
@RequestMapping("/mps")
public class MPSController {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private LineFeign lineFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MPSVo>> selectByPage(@RequestBody MPSViewPageQuery dto) {
        if (StringUtils.isNotEmpty(dto.getProductName()) || StringUtils.isNotEmpty(dto.getProductCode())) {
            MOPageQuery query = new MOPageQuery()
                    .setProductCode(dto.getProductCode())
                    .setProductName(dto.getProductName());
            query.setRequirePage(false);
            List<MODto> mos;
            try {
                mos = RemoteHandleUtils.getDataFormResponse(moFeign.selectByPage(query));
            } catch (EraCommonException e) {
                throw new BusinessException(e.getMessage());
            }
            if (CollectionUtil.isBlank(mos)) {
                return JsonResponseEntity.success(new ArrayList<>(), new Pagination(0, dto.getPage(), dto.getPageSize()));
            }
            List<String> mocodes = mos.stream().map(MODto::getSelfCode).collect(Collectors.toList());
            dto.setMoCodes(mocodes);
        }
        JsonResponseEntity<List<MPSDto>> page = mpsFeign.selectByPage(dto);
        List<MPSVo> r = MPSVo.wrap(page.getData());
        if (CollectionUtil.isBlank(r)) {
            return JsonResponseEntity.success(r, page.getPagination());
        }

        List<MODto> mos = null;
        List<LineVo> lines = null;
        if (dto.getQueryMO()) {
            List<String> moCodes = r.stream().map(MPSDto::getMoCode).collect(Collectors.toList());
            try {
                mos = RemoteHandleUtils.getDataFormResponse(moFeign.selectByCodes(moCodes));
            } catch (EraCommonException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        if (dto.getQueryLine()) {
            List<String> lineCodes = r.stream().map(MPSDto::getLine).collect(Collectors.toList());
            try {
                lines = RemoteHandleUtils.getDataFormResponse(lineFeign.selectByCodes(lineCodes));
            } catch (EraCommonException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        for (MPSVo mpsDto : r) {
            if (CollectionUtil.isNotBlank(mos)) {
                for (MODto mo : mos) {
                    if (mpsDto.getMoCode().equals(mo.getSelfCode())) {
                        mpsDto.setMoDto(mo);
                        break;
                    }
                }
            }
            if (CollectionUtil.isNotBlank(lines)) {
                for (LineVo line : lines) {
                    if (mpsDto.getLine().equals(line.getSelfCode())) {
                        mpsDto.setLineVo(line);
                        break;
                    }
                }
            }
        }
        return JsonResponseEntity.success(r, page.getPagination());
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<MPSVo> selectById(@RequestParam String id) {
        try {
            MPSDto d = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectById(id));
            MPSVo mps = MPSVo.wrap(d);
            if (mps != null) {
                mps.setLineVo(RemoteHandleUtils.getDataFormResponse(lineFeign.selectByCode(mps.getLine())));
                mps.setMoDto(RemoteHandleUtils.getDataFormResponse(moFeign.selectByCode(mps.getMoCode())));
            }
            return JsonResponseEntity.success(mps);
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDto dto) {
        return mpsFeign.addOrUpdate(dto);
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<MPSDto> dtos) {
        return mpsFeign.addBatch(dtos);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return mpsFeign.delete(idsDto);
    }

    @Operation(summary = "改变优先级", description = "改变优先级")
    @PostMapping("changePriority")
    public JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto) {
        return mpsFeign.changePriority(dto);
    }

    @Operation(summary = "批量加入生产队列", description = "批量加入生产队列")
    @PostMapping("joinQueueBatch")
    public JsonResponseEntity<Boolean> joinQueueBatch(@Validated @RequestBody List<String> ids) {
        return mpsFeign.joinQueueBatch(ids);
    }

    @Operation(summary = "加入生产队列", description = "加入生产队列")
    @PostMapping("joinQueue")
    public JsonResponseEntity<Boolean> joinQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        return mpsFeign.joinQueue(dto);
    }

    @Operation(summary = "插队", description = "插队")
    @PostMapping("jumpQueue")
    public JsonResponseEntity<Boolean> jumpQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        return mpsFeign.jumpQueue(dto);
    }

    @Operation(summary = "移出生产队列", description = "移出生产队列")
    @PostMapping("removeQueue")
    public JsonResponseEntity<Boolean> removeQueue(@Validated @RequestBody MPSQueueOperateDto dto) {
        return mpsFeign.removeQueue(dto);
    }

    @Operation(summary = "流程开始", description = "流程开始")
    @GetMapping("startTf")
    public JsonResponseEntity<Boolean> startTf(@RequestParam String tfId) {
        return mpsFeign.startTf(tfId);
    }

    @Operation(summary = "状态", description = "状态")
    @GetMapping("status")
    public JsonResponseEntity<List<MapDto>> status() {
        List<MapDto> r = new ArrayList<>();
        for (MPSStatusEnum value : MPSStatusEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }
}
