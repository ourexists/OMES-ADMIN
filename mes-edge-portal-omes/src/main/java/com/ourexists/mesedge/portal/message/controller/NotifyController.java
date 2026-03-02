/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.message.controller;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.message.enums.MessageSourceEnum;
import com.ourexists.mesedge.message.enums.MessageTypeEnum;
import com.ourexists.mesedge.message.enums.NotifyStatusEnum;
import com.ourexists.mesedge.message.feign.NotifyFeign;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.message.model.NotifyVo;
import com.ourexists.mesedge.message.model.query.NotifyPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知")
@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private NotifyFeign feign;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<NotifyVo>> selectByPage(@RequestBody NotifyPageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody NotifyDto dto) {
        try {
            Boolean r = RemoteHandleUtils.getDataFormResponse(feign.addOrUpdate(dto));
            return JsonResponseEntity.success(r);
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }


    @Operation(summary = "开启", description = "")
    @GetMapping("start")
    public JsonResponseEntity<Boolean> start(@RequestParam String id) {
        return feign.updateStatus(id, NotifyStatusEnum.PROGRESS.getCode());
    }

    @Operation(summary = "完成", description = "")
    @GetMapping("complete")
    public JsonResponseEntity<Boolean> complete(@RequestParam String id) {
        return feign.updateStatus(id, NotifyStatusEnum.COMPLETED.getCode());
    }


    @Operation(summary = "通知状态", description = "")
    @GetMapping("notifyStatus")
    public JsonResponseEntity<Map<Integer, String>> notifyStatus() {
        Map<Integer, String> m = new HashMap<>();
        for (NotifyStatusEnum value : NotifyStatusEnum.values()) {
            m.put(value.getCode(), value.name());
        }
        return JsonResponseEntity.success(m);
    }

    @Operation(summary = "消息类型", description = "")
    @GetMapping("messageTypes")
    public JsonResponseEntity<Map<Integer, String>> messageTypes() {
        Map<Integer, String> m = new HashMap<>();
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            m.put(value.getCode(), value.name());
        }
        return JsonResponseEntity.success(m);
    }

    @Operation(summary = "消息来源", description = "")
    @GetMapping("messageSources")
    public JsonResponseEntity<Map<String, String>> messageSources() {
        Map<String, String> m = new HashMap<>();
        for (MessageSourceEnum value : MessageSourceEnum.values()) {
            m.put(value.name(), value.name());
        }
        return JsonResponseEntity.success(m);
    }
}
