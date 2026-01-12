/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.message.feign.NotifyFeign;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.message.model.NotifyVo;
import com.ourexists.mesedge.message.model.query.NotifyPageQuery;
import com.ourexists.mesedge.message.pojo.Notify;
import com.ourexists.mesedge.message.service.NotifyService;
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
public class NotifyViewer implements NotifyFeign {

    @Autowired
    private NotifyService service;

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    public JsonResponseEntity<List<NotifyVo>> selectByPage(@RequestBody NotifyPageQuery dto) {
        Page<Notify> page = service.selectByPage(dto);
        return JsonResponseEntity.success(Notify.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody NotifyDto dto) {
        service.addOrUpdate(Notify.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<Boolean> updateStatus(@RequestParam String id, @RequestParam Integer status) {
        service.updateStatus(id, status);
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<Boolean> createAndStart(@Validated @RequestBody NotifyDto dto) {
        service.createAndStart(dto);
        return JsonResponseEntity.success(true);
    }
}
