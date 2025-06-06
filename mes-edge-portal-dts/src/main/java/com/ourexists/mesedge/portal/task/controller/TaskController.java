/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.task.feign.TaskFeign;
import com.ourexists.mesedge.task.model.TaskDto;
import com.ourexists.mesedge.task.model.TaskVo;
import com.ourexists.mesedge.task.model.query.TaskPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskFeign taskFeign;


    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<TaskVo>> selectByPage(@RequestBody TaskPageQuery dto) {
        return taskFeign.selectByPage(dto);
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<TaskVo> selectById(@RequestParam String id) {
        return taskFeign.selectById(id);
    }

    @Operation(summary = "启用", description = "启用")
    @GetMapping("start")
    public JsonResponseEntity<Boolean> start(@RequestParam String id) {
        return taskFeign.start(id);
    }

    @Operation(summary = "停用", description = "停用")
    @GetMapping("stop")
    public JsonResponseEntity<Boolean> stop(@RequestParam String id) {
        return taskFeign.stop(id);
    }

    @Operation(summary = "新增更新", description = "新增更新")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TaskDto dto) {
        return taskFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return taskFeign.delete(idsDto);
    }

    @Operation(summary = "所有的定时任务", description = "所有的定时任务")
    @GetMapping("timerTask")
    public JsonResponseEntity<List<MapDto>> timerTask() {
        List<MapDto> r = new ArrayList<>();
        try {
            Set<String> taskNames = RemoteHandleUtils.getDataFormResponse(taskFeign.getAllTimerTask());
            if (CollectionUtil.isNotBlank(taskNames)) {
                for (String s : taskNames) {
                    r.add(new MapDto().setId(s).setName(s));
                }
            }
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        return JsonResponseEntity.success(r);
    }
}
