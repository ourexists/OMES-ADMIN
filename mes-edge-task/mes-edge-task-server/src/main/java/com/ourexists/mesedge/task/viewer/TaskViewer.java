/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.task.feign.TaskFeign;
import com.ourexists.mesedge.task.model.TaskDto;
import com.ourexists.mesedge.task.model.TaskVo;
import com.ourexists.mesedge.task.model.query.TaskPageQuery;
import com.ourexists.mesedge.task.pojo.Task;
import com.ourexists.mesedge.task.process.TimerTaskManager;
import com.ourexists.mesedge.task.service.TaskService;
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
public class TaskViewer {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TimerTaskManager timerTaskManager;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<TaskVo>> selectByPage(@RequestBody TaskPageQuery dto) {
        Page<Task> page = taskService.selectByPage(dto);
        return JsonResponseEntity.success(Task.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<TaskVo> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Task.covert(taskService.getById(id)));
    }

    @Operation(summary = "启用", description = "启用")
    @GetMapping("start")
    public JsonResponseEntity<Boolean> start(@RequestParam String id) {
        taskService.start(id);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "停用", description = "停用")
    @GetMapping("stop")
    public JsonResponseEntity<Boolean> stop(@RequestParam String id) {
        taskService.stop(id);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "新增更新", description = "新增更新")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TaskDto dto) {
        taskService.addOrUpdate(Task.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        for (String id : idsDto.getIds()) {
            taskService.delete(id);
        }
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "所有的定时任务2", description = "所有的定时任务")
    @GetMapping("getAllTimerTask")
    public JsonResponseEntity<Set<String>> getAllTimerTask() {
        return JsonResponseEntity.success(timerTaskManager.getAllTimerTask().keySet());
    }

    @Operation(summary = "所有的定时任务", description = "所有的定时任务")
    @GetMapping("timerTask")
    public JsonResponseEntity<List<MapDto>> timerTask() {
        List<MapDto> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(timerTaskManager.getAllTimerTask().keySet())) {
            for (String s : timerTaskManager.getAllTimerTask().keySet()) {
                r.add(new MapDto().setId(s).setName(s));
            }
        }
        return JsonResponseEntity.success(r);
    }
}
