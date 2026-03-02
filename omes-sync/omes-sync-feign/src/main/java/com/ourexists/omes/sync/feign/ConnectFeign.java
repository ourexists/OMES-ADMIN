/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.feign;


import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.sync.model.ConnectDto;
import com.ourexists.omes.sync.model.query.ConnectPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ConnectFeign {

    JsonResponseEntity<List<ConnectDto>> selectConnectByProtocol(@RequestParam String protocol);

    JsonResponseEntity<ConnectDto> selectConnectByName(@RequestParam String serverName);

    @PostMapping("selectByPage")
    JsonResponseEntity<List<ConnectDto>> selectByPage(@RequestBody ConnectPageQuery query);

    @GetMapping("selectById")
    JsonResponseEntity<ConnectDto> selectById(@RequestParam String id);

    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ConnectDto dto);

    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
