/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mps.enums.MPSStatusEnum;
import com.ourexists.mesedge.mps.model.ChangePriorityDto;
import com.ourexists.mesedge.mps.model.MPSDto;
import com.ourexists.mesedge.mps.model.MPSQueueOperateDto;
import com.ourexists.mesedge.mps.model.query.MPSPageQuery;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "生产计划")
//@RestController
//@RequestMapping("/mps")
public interface MPSFeign {

    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<MPSDto>> selectByPage(@RequestBody MPSPageQuery dto);

    //    @Operation(summary = "id查詢", description = "id查詢")
//    @GetMapping("selectById")
    JsonResponseEntity<MPSDto> selectById(@RequestParam String id);

    JsonResponseEntity<MPSDto> selectByCode(@RequestParam String code);

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDto dto);

    //    @Operation(summary = "批量新增", description = "批量新增")
//    @PostMapping("addBatch")
    JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<MPSDto> dtos);

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    //    @Operation(summary = "改变优先级", description = "改变优先级")
//    @PostMapping("changePriority")
    JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto);

    //    @Operation(summary = "批量加入生产队列", description = "批量加入生产队列")
//    @PostMapping("joinQueueBatch")
    JsonResponseEntity<Boolean> joinQueueBatch(@Validated @RequestBody List<String> ids);

    //    @Operation(summary = "加入生产队列", description = "加入生产队列")
//    @PostMapping("joinQueue")
    JsonResponseEntity<Boolean> joinQueue(@Validated @RequestBody MPSQueueOperateDto dto);

    //    @Operation(summary = "插队", description = "插队")
//    @PostMapping("jumpQueue")
    JsonResponseEntity<Boolean> jumpQueue(@Validated @RequestBody MPSQueueOperateDto dto);

    //    @Operation(summary = "移出生产队列", description = "移出生产队列")
//    @PostMapping("removeQueue")
    JsonResponseEntity<Boolean> removeQueue(@Validated @RequestBody MPSQueueOperateDto dto);

    //    @Operation(summary = "流程开始", description = "流程开始")
//    @GetMapping("startTf")
    JsonResponseEntity<Boolean> startTf(@RequestParam String tfId);

    JsonResponseEntity<Long> countExecByCode(@NotBlank String lineCode);

    JsonResponseEntity<Integer> getMaxBatch(@RequestParam String selfCode);

    JsonResponseEntity<List<MPSDto>> selectByStatus(@RequestParam MPSStatusEnum mpsStatusEnum);

    JsonResponseEntity<List<MPSDto>> selectEnabledJoinQueMps();

    JsonResponseEntity<Boolean> adjustToJoinQue();

    JsonResponseEntity<Boolean> joinQueueBatchByMoCodesLimitEnable(@RequestBody List<String> moCodes);
}
