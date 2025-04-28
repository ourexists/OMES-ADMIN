/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.feign;


import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.sync.model.ConnectDto;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ConnectFeign {

    //    @Operation(summary = "所有协议", description = "")
//    @GetMapping("getAll")
    JsonResponseEntity<List<ConnectDto>> selectConnectByProtocol(@RequestParam String protocol);

    JsonResponseEntity<ConnectDto> selectConnectByName(@RequestParam String serverName);
}
