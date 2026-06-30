/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.mps.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.model.vo.Pagination;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.line.feign.LineFeign;
import com.ourexists.omes.line.model.LineVo;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mo.model.query.MOPageQuery;
import com.ourexists.omes.mps.enums.MPSStatusEnum;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.ChangePriorityDto;
import com.ourexists.omes.mps.model.MPSBoardDto;
import com.ourexists.omes.mps.model.MPSDto;
import com.ourexists.omes.mps.model.MPSQueueOperateDto;
import com.ourexists.omes.mps.model.query.MPSBoardQuery;
import com.ourexists.omes.mps.model.query.MPSPageQuery;
import com.ourexists.omes.portal.mps.model.MPSBoardViewQuery;
import com.ourexists.omes.portal.mps.model.MPSBoardVo;
import com.ourexists.omes.portal.mps.model.MPSViewPageQuery;
import com.ourexists.omes.portal.mps.model.MPSVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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

    @Operation(summary = "看板聚合查询", description = "一次返回待排产、执行队列、执行中、已完成四列数据")
    @PostMapping("selectBoard")
    public JsonResponseEntity<MPSBoardVo> selectBoard(@RequestBody MPSBoardViewQuery query) {
        MPSBoardQuery boardQuery = new MPSBoardQuery()
                .setMoCode(query.getMoCode())
                .setLimitPerColumn(query.getLimitPerColumn());
        applyProductFilter(query.getProductName(), query.getProductCode(), boardQuery);
        if (boardQuery.getMoCodes() != null && boardQuery.getMoCodes().isEmpty()) {
            return JsonResponseEntity.success(new MPSBoardVo());
        }

        MPSBoardDto board;
        try {
            board = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectBoard(boardQuery));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (board == null) {
            return JsonResponseEntity.success(new MPSBoardVo());
        }

        MPSBoardVo result = new MPSBoardVo()
                .setWaitQue(MPSVo.wrap(board.getWaitQue()))
                .setWaitExec(MPSVo.wrap(board.getWaitExec()))
                .setExecing(MPSVo.wrap(board.getExecing()))
                .setComplete(MPSVo.wrap(board.getComplete()));

        List<MPSVo> all = new ArrayList<>();
        all.addAll(result.getWaitQue());
        all.addAll(result.getWaitExec());
        all.addAll(result.getExecing());
        all.addAll(result.getComplete());
        enrichMpsVoList(all, query.getQueryMO(), query.getQueryLine());
        return JsonResponseEntity.success(result);
    }

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MPSVo>> selectByPage(@RequestBody MPSViewPageQuery dto) {
        applyProductFilter(dto.getProductName(), dto.getProductCode(), dto);
        if (dto.getMoCodes() != null && dto.getMoCodes().isEmpty()) {
            return JsonResponseEntity.success(new ArrayList<>(), new Pagination(0, dto.getPage(), dto.getPageSize()));
        }
        JsonResponseEntity<List<MPSDto>> page = mpsFeign.selectByPage(dto);
        List<MPSVo> r = MPSVo.wrap(page.getData());
        if (CollectionUtil.isBlank(r)) {
            return JsonResponseEntity.success(r, page.getPagination());
        }
        enrichMpsVoList(r, dto.getQueryMO(), dto.getQueryLine());
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

    private void applyProductFilter(String productName, String productCode, MPSPageQuery dto) {
        if (StringUtils.isEmpty(productName) && StringUtils.isEmpty(productCode)) {
            return;
        }
        MOPageQuery query = new MOPageQuery()
                .setProductCode(productCode)
                .setProductName(productName);
        query.setRequirePage(false);
        List<MODto> mos;
        try {
            mos = RemoteHandleUtils.getDataFormResponse(moFeign.selectByPage(query));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (CollectionUtil.isBlank(mos)) {
            dto.setMoCodes(Collections.emptyList());
            return;
        }
        dto.setMoCodes(mos.stream().map(MODto::getSelfCode).distinct().collect(Collectors.toList()));
    }

    private void applyProductFilter(String productName, String productCode, MPSBoardQuery query) {
        if (StringUtils.isEmpty(productName) && StringUtils.isEmpty(productCode)) {
            return;
        }
        MOPageQuery moQuery = new MOPageQuery()
                .setProductCode(productCode)
                .setProductName(productName);
        moQuery.setRequirePage(false);
        List<MODto> mos;
        try {
            mos = RemoteHandleUtils.getDataFormResponse(moFeign.selectByPage(moQuery));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (CollectionUtil.isBlank(mos)) {
            query.setMoCodes(Collections.emptyList());
            return;
        }
        query.setMoCodes(mos.stream().map(MODto::getSelfCode).distinct().collect(Collectors.toList()));
    }

    private void enrichMpsVoList(List<MPSVo> records, Boolean queryMO, Boolean queryLine) {
        if (CollectionUtil.isBlank(records)) {
            return;
        }
        Map<String, MODto> moMap = null;
        Map<String, LineVo> lineMap = null;
        if (Boolean.TRUE.equals(queryMO)) {
            List<String> moCodes = records.stream()
                    .map(MPSDto::getMoCode)
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotBlank(moCodes)) {
                try {
                    List<MODto> mos = RemoteHandleUtils.getDataFormResponse(moFeign.selectByCodes(moCodes));
                    moMap = toMap(mos, MODto::getSelfCode);
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        }
        if (Boolean.TRUE.equals(queryLine)) {
            List<String> lineCodes = records.stream()
                    .map(MPSDto::getLine)
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotBlank(lineCodes)) {
                try {
                    List<LineVo> lines = RemoteHandleUtils.getDataFormResponse(lineFeign.selectByCodes(lineCodes));
                    lineMap = toMap(lines, LineVo::getSelfCode);
                } catch (EraCommonException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        }
        for (MPSVo record : records) {
            if (moMap != null && StringUtils.isNotEmpty(record.getMoCode())) {
                record.setMoDto(moMap.get(record.getMoCode()));
            }
            if (lineMap != null && StringUtils.isNotEmpty(record.getLine())) {
                record.setLineVo(lineMap.get(record.getLine()));
            }
            if (record.getStatus() != null) {
                record.setStatusDesc(MPSStatusEnum.valueOf(record.getStatus()).getName());
            }
        }
    }

    private <T> Map<String, T> toMap(List<T> list, Function<T, String> keyExtractor) {
        if (CollectionUtil.isBlank(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotEmpty(keyExtractor.apply(item)))
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (left, right) -> left));
    }
}
