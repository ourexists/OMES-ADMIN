/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.sync.enums.ProtocolEnum;
import com.ourexists.mesedge.sync.model.ConnectDto;
import com.ourexists.mesedge.sync.pojo.Connect;
import com.ourexists.mesedge.sync.service.ConnectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "连接管理")
@RestController
@RequestMapping("/connect")
public class ConnectViewer {

    @Autowired
    private ConnectService connectService;

    @Operation(summary = "所有协议", description = "")
    @GetMapping("getAllConnect")
    public JsonResponseEntity<List<ConnectDto>> selectConnectByProtocol(@RequestParam String protocol) {
        List<Connect> connects = connectService.getConnectByProtocol(ProtocolEnum.valueOf(protocol));
        return JsonResponseEntity.success(Connect.covert(connects));
    }

    @Operation(summary = "通过名称查询连接", description = "")
    @GetMapping("selectConnectByName")
    public JsonResponseEntity<ConnectDto> selectConnectByName(@RequestParam String serverName) {
        Connect connect = connectService.getConnect(serverName);
        return JsonResponseEntity.success(Connect.covert(connect));
    }

    @Operation(summary = "opcUa协议连接", description = "")
    @GetMapping("opcUa")
    public JsonResponseEntity<List<MapDto>> opcUa() {
        List<Connect> connects = connectService.getConnectByProtocol(ProtocolEnum.valueOf(ProtocolEnum.opc_ua.name()));
        List<MapDto> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(connects)) {
            for (Connect Connect : connects) {
                r.add(new MapDto().setId(Connect.getServerName()).setName(Connect.getServerName()));
            }
        }
        return JsonResponseEntity.success(r);
    }
}
