/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.message.feign.MessageFeign;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import com.ourexists.mesedge.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "生产订单")
//@RestController
//@RequestMapping("/mo")
@Component
public class MessageViewer implements MessageFeign {

    @Autowired
    private MessageService service;

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MessageVo>> selectByPage(@RequestBody MessagePageQuery dto) {
        Page<MessageVo> page = service.selectByPage(dto);
        return JsonResponseEntity.success(page.getRecords(), OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<MessageVo> produce(@Validated @RequestBody MessageDto dto) {
        return JsonResponseEntity.success(service.produce(dto));
    }

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.deleteByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<Boolean> read(@RequestParam String messageId) {
        service.read(messageId);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<MessageVo> selectById(@RequestParam String id, @RequestParam String accId) {
        return JsonResponseEntity.success(service.selectById(id, accId));
    }
}
