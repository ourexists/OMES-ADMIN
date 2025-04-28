/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.sync.enums.ProtocolEnum;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import com.ourexists.mesedge.sync.pojo.Connect;
import com.ourexists.mesedge.sync.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "plc连接管理")
//@RestController
//@RequestMapping("/plc")
@Component
public class ConnectViewer implements ConnectFeign {

    @Autowired
    private ConnectService connectService;

    //    @Operation(summary = "所有协议", description = "")
//    @GetMapping("getAll")
    public JsonResponseEntity<List<ConnectDto>> selectConnectByProtocol(@RequestParam String protocol) {
        List<Connect> connects = connectService.getConnectByProtocol(ProtocolEnum.valueOf(protocol));
        return JsonResponseEntity.success(Connect.covert(connects));
    }

    @Override
    public JsonResponseEntity<ConnectDto> selectConnectByName(@RequestParam String serverName) {
        Connect connect = connectService.getConnect(serverName);
        return JsonResponseEntity.success(Connect.covert(connect));
    }
}
