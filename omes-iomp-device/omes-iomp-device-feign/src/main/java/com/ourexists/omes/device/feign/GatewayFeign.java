/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;


import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.GatewayDto;
import com.ourexists.omes.device.model.GatewayPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GatewayFeign {

    JsonResponseEntity<List<GatewayDto>> selectConnectByProtocol(@RequestParam String protocol);

    JsonResponseEntity<GatewayDto> selectConnectByName(@RequestParam String serverName);

    @PostMapping("selectByPage")
    JsonResponseEntity<List<GatewayDto>> selectByPage(@RequestBody GatewayPageQuery query);

    @GetMapping("selectById")
    JsonResponseEntity<GatewayDto> selectById(@RequestParam String id);

    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody GatewayDto dto);

    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    @GetMapping("start")
    JsonResponseEntity<Boolean> start(@RequestParam String id);

    @GetMapping("stop")
    JsonResponseEntity<Boolean> stop(@RequestParam String id);
}
