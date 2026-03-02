/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.sync.model.DeviceCollectBindingDto;
import com.ourexists.mesedge.sync.model.ConnectIdAndSourceKeysRequest;
import com.ourexists.mesedge.sync.model.query.DeviceCollectBindingPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DeviceCollectBindingFeign {

    @PostMapping("selectByPage")
    JsonResponseEntity<List<DeviceCollectBindingDto>> selectByPage(@RequestBody DeviceCollectBindingPageQuery query);

    @GetMapping("selectById")
    JsonResponseEntity<DeviceCollectBindingDto> selectById(@RequestParam String id);

    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceCollectBindingDto dto);

    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    @PostMapping("listByConnectIdAndSourceKeys")
    JsonResponseEntity<List<DeviceCollectBindingDto>> listByConnectIdAndSourceKeys(@RequestBody ConnectIdAndSourceKeysRequest request);
}
