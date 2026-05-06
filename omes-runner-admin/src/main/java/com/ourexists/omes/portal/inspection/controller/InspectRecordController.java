/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.inspection.feign.InspectRecordFeign;
import com.ourexists.omes.inspection.model.InspectRecordDto;
import com.ourexists.omes.inspection.model.InspectRecordPageQuery;
import com.ourexists.omes.inspection.model.InspectRecordSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 巡检记录
 */
@Tag(name = "巡检记录")
@RestController
@RequestMapping("/inspection/record")
public class InspectRecordController {

    @Autowired
    private InspectRecordFeign feign;
    @Autowired
    private EquipFeign equipFeign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectRecordDto>> selectByPage(@RequestBody InspectRecordPageQuery query) {
        JsonResponseEntity<List<InspectRecordDto>> resp = feign.selectByPage(query);
        if (resp != null && !CollectionUtils.isEmpty(resp.getData())) {
            fillEquipNames(resp.getData());
        }
        return resp;
    }

    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectRecordDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "生成巡检记录（APP 提交）")
    @PostMapping("save")
    public JsonResponseEntity<Boolean> save(@Validated @RequestBody InspectRecordSaveRequest request) {
        return feign.save(request);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectRecordDto> selectById(@RequestParam String id) {
        JsonResponseEntity<InspectRecordDto> resp = feign.selectById(id);
        if (resp != null && resp.getData() != null) {
            fillEquipNames(Collections.singletonList(resp.getData()));
        }
        return resp;
    }

    @Operation(summary = "按任务ID查询记录列表")
    @GetMapping("listByTaskId")
    public JsonResponseEntity<List<InspectRecordDto>> listByTaskId(@RequestParam String taskId) {
        JsonResponseEntity<List<InspectRecordDto>> resp = feign.listByTaskId(taskId);
        if (resp != null && !CollectionUtils.isEmpty(resp.getData())) {
            fillEquipNames(resp.getData());
        }
        return resp;
    }

    @Operation(summary = "巡检结果枚举")
    @GetMapping("resultTypes")
    public JsonResponseEntity<Map<Integer, String>> resultTypes() {
        return JsonResponseEntity.success(Map.of(
                0, "正常",
                1, "异常"
        ));
    }

    /** 批量关联查询设备名称并回填，避免循环调用 */
    private void fillEquipNames(List<InspectRecordDto> list) {
        if (list == null || list.isEmpty()) return;
        List<String> equipIds = list.stream()
                .map(InspectRecordDto::getEquipId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (equipIds.isEmpty()) return;
        List<EquipDto> equips;
        try {
            equips = RemoteHandleUtils.getDataFormResponse(equipFeign.listByIds(equipIds));
            if (equips == null) return;
            Map<String, String> equipIdToName = equips.stream()
                    .filter(e -> e.getId() != null)
                    .collect(Collectors.toMap(EquipDto::getId, e -> e.getName() != null ? e.getName() : "", (a, b) -> a));
            Map<String, String> equipIdToSelfCode = equips.stream()
                    .filter(e -> e.getId() != null)
                    .collect(Collectors.toMap(EquipDto::getId, e -> e.getSelfCode() != null ? e.getSelfCode() : "", (a, b) -> a));
            for (InspectRecordDto dto : list) {
                if (dto.getEquipId() != null) {
                    if (equipIdToName.containsKey(dto.getEquipId())) {
                        dto.setEquipName(equipIdToName.get(dto.getEquipId()));
                    }
                    if (equipIdToSelfCode.containsKey(dto.getEquipId())) {
                        dto.setEquipSelfCode(equipIdToSelfCode.get(dto.getEquipId()));
                    }
                }
            }
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
