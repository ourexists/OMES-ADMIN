/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.task.model.TaskDto;
import com.ourexists.mesedge.task.model.TaskVo;
import com.ourexists.mesedge.task.model.query.TaskPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

//@RequestMapping("/task")
public interface TaskFeign {


    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<TaskVo>> selectByPage(@RequestBody TaskPageQuery dto);

    //    @Operation(summary = "id查詢", description = "id查詢")
//    @GetMapping("selectById")
    JsonResponseEntity<TaskVo> selectById(@RequestParam String id);


    JsonResponseEntity<TaskVo> selectByType(@RequestParam String type);

    //    @Operation(summary = "启用", description = "启用")
//    @GetMapping("start")
    JsonResponseEntity<Boolean> start(@RequestParam String id);

    //    @Operation(summary = "停用", description = "停用")
//    @GetMapping("stop")
    JsonResponseEntity<Boolean> stop(@RequestParam String id);

    //    @Operation(summary = "新增更新", description = "新增更新")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TaskDto dto);

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<Set<String>> getAllTimerTask();
}
