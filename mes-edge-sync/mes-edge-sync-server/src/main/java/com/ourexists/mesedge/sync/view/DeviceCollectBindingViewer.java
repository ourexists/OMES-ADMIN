/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.sync.feign.DeviceCollectBindingFeign;
import com.ourexists.mesedge.sync.model.ConnectIdAndSourceKeysRequest;
import com.ourexists.mesedge.sync.model.DeviceCollectBindingDto;
import com.ourexists.mesedge.sync.model.query.DeviceCollectBindingPageQuery;
import com.ourexists.mesedge.sync.pojo.DeviceCollectBinding;
import com.ourexists.mesedge.sync.service.DeviceCollectBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Component
public class DeviceCollectBindingViewer implements DeviceCollectBindingFeign {

    @Autowired
    private DeviceCollectBindingService bindingService;

    @Override
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<DeviceCollectBindingDto>> selectByPage(@RequestBody DeviceCollectBindingPageQuery query) {
        Page<DeviceCollectBinding> page = bindingService.selectByPage(query);
        return JsonResponseEntity.success(DeviceCollectBinding.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Override
    @GetMapping("selectById")
    public JsonResponseEntity<DeviceCollectBindingDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(DeviceCollectBinding.covert(bindingService.getById(id)));
    }

    @Override
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceCollectBindingDto dto) {
        bindingService.saveOrUpdate(DeviceCollectBinding.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Override
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        bindingService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @PostMapping("listByConnectIdAndSourceKeys")
    public JsonResponseEntity<List<DeviceCollectBindingDto>> listByConnectIdAndSourceKeys(@RequestBody ConnectIdAndSourceKeysRequest request) {
        if (request == null || request.getSourceKeys() == null || request.getSourceKeys().isEmpty()) {
            return JsonResponseEntity.success(Collections.emptyList());
        }
        List<DeviceCollectBinding> list = bindingService.listByConnectIdAndSourceKeys(request.getConnectId(), request.getSourceKeys());
        return JsonResponseEntity.success(DeviceCollectBinding.covert(list));
    }
}
