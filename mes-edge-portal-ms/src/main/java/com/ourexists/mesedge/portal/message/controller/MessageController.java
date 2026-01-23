/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.message.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.message.feign.MessageFeign;
import com.ourexists.mesedge.message.model.MessageVo;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import com.ourexists.mesedge.portal.message.SinkManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Slf4j
@Tag(name = "消息")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageFeign feign;

    @Autowired
    private SinkManager sinkManager;

    @GetMapping(value = "/listen", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> listen() {
        Sinks.Many<String> sink = sinkManager.getSink(UserContext.getPlatForm());
        return sink.asFlux()
//                .map(msg -> "data: " + msg + "\n\n")
                .doOnCancel(() -> {
                    log.info("client stopped.time[{}]", new Date());
                });
    }

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MessageVo>> selectByPage(@RequestBody MessagePageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "分页", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<MessageVo> selectById(@RequestParam String id, @RequestParam String accId) {
        return feign.selectById(id, accId);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "已读", description = "")
    @GetMapping("read")
    public JsonResponseEntity<Boolean> read(@RequestParam String messageId) {
        return feign.read(messageId);
    }

    @Operation(summary = "连接实时通道", description = "")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> connect(@RequestParam String clientId) {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "message " + sequence);
    }

    @Operation(summary = "统计未读", description = "")
    @GetMapping(value = "/countUnread")
    public JsonResponseEntity<Long> countUnread() {
        return feign.countReadStatus(UserContext.getUser().getId(), UserContext.getPlatForm(), 0);
    }
}
