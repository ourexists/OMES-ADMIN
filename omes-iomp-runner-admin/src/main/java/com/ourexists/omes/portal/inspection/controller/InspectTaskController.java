/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.WorkshopTreeNode;
import com.ourexists.omes.inspection.enums.InspectTaskStatusEnum;
import com.ourexists.omes.inspection.feign.InspectPersonFeign;
import com.ourexists.omes.inspection.feign.InspectTaskFeign;
import com.ourexists.omes.inspection.model.InspectPersonDto;
import com.ourexists.omes.inspection.model.InspectTaskDto;
import com.ourexists.omes.inspection.model.InspectTaskPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 巡检任务
 */
@Slf4j
@Tag(name = "巡检任务")
@RestController
@RequestMapping("/inspection/task")
public class InspectTaskController {

    @Autowired
    private InspectTaskFeign feign;
    @Autowired
    private WorkshopFeign workshopFeign;
    @Autowired
    private InspectPersonFeign inspectPersonFeign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectTaskDto>> selectByPage(@RequestBody InspectTaskPageQuery query) {
        JsonResponseEntity<List<InspectTaskDto>> res = feign.selectByPage(query);
        fillWorkshopName(res.getData());
        return res;
    }

    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTaskDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectTaskDto> selectById(@RequestParam String id) {
        JsonResponseEntity<InspectTaskDto> res = feign.selectById(id);
        if (res.getData() != null) fillWorkshopName(List.of(res.getData()));
        return res;
    }

    @Operation(summary = "按计划ID查询任务列表")
    @GetMapping("listByPlanId")
    public JsonResponseEntity<List<InspectTaskDto>> listByPlanId(@RequestParam String planId) {
        JsonResponseEntity<List<InspectTaskDto>> res = feign.listByPlanId(planId);
        fillWorkshopName(res.getData());
        return res;
    }

    /** 根据本页任务中的 workshopCode 在 portal 层拼接 workshopName（调用场景接口解析） */
    private void fillWorkshopName(List<InspectTaskDto> list) {
        if (CollectionUtils.isEmpty(list)) return;
        List<String> codes = list.stream()
                .map(InspectTaskDto::getWorkshopCode)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();
        if (codes.isEmpty()) return;
        Map<String, String> codeToName = resolveWorkshopCodeToName(codes);
        for (InspectTaskDto dto : list) {
            dto.setWorkshopName(codeToName.getOrDefault(dto.getWorkshopCode(), ""));
        }
    }

    private Map<String, String> resolveWorkshopCodeToName(List<String> codes) {
        Map<String, String> map = new HashMap<>();
        try {
            List<WorkshopTreeNode> treeRes = RemoteHandleUtils.getDataFormResponse(workshopFeign.selectByCodes(codes));
            for (WorkshopTreeNode treeRe : treeRes) {
                map.put(treeRe.getSelfCode(), treeRe.getName());
            }
            return map;
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "任务指派：将任务指派给指定巡检人员")
    @PostMapping("assign")
    public JsonResponseEntity<Boolean> assign(@RequestBody InspectTaskAssignRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getTaskIds()) || request.getPersonId() == null || request.getPersonId().isBlank()) {
            throw new BusinessException("请选择任务和指派的巡检人员");
        }
        JsonResponseEntity<InspectPersonDto> personRes = inspectPersonFeign.selectById(request.getPersonId());
        if (personRes == null || personRes.getData() == null) {
            throw new BusinessException("巡检人员不存在");
        }
        InspectPersonDto person = personRes.getData();
        String executorName = person.getName() != null ? person.getName() : person.getJobNumber();
        if (executorName == null || executorName.isBlank()) executorName = person.getId();
        int assigned = 0;
        for (String taskId : request.getTaskIds()) {
            JsonResponseEntity<InspectTaskDto> taskRes = feign.selectById(taskId);
            if (taskRes != null && taskRes.getData() != null) {
                InspectTaskDto dto = taskRes.getData();
                if (dto.getStatus() == null || dto.getStatus() != InspectTaskStatusEnum.PENDING.getCode()) {
                    continue; // 只有待执行才能指派，跳过非待执行
                }
                dto.setExecutorPersonId(person.getId());
                dto.setExecutorId(person.getAccountId());
                dto.setExecutorName(executorName);
                feign.addOrUpdate(dto);
                assigned++;
            }
        }
        if (assigned == 0) {
            throw new BusinessException("只有待执行的任务才能指派，所选任务中无待执行任务");
        }
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "重新开始：将已逾期的任务重置为待执行")
    @PostMapping("restartOverdue")
    public JsonResponseEntity<Boolean> restartOverdue(@RequestBody IdsDto idsDto) {
        if (idsDto == null || CollectionUtils.isEmpty(idsDto.getIds())) {
            throw new BusinessException("请选择要重新开始的任务");
        }
        JsonResponseEntity<Boolean> res = feign.restartOverdue(idsDto);
        if (res == null || !Boolean.TRUE.equals(res.getData())) {
            throw new BusinessException("所选任务中无已逾期任务或操作失败");
        }
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "任务执行状态枚举（后端统一管理）")
    @GetMapping("statusTypes")
    public JsonResponseEntity<Map<Integer, String>> statusTypes() {
        return JsonResponseEntity.success(InspectTaskStatusEnum.toMap());
    }
}
