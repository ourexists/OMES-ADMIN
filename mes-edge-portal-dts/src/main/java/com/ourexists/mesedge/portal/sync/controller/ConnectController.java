/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.sync.enums.ProtocolEnum;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "连接管理")
@RestController
@RequestMapping("/connect")
public class ConnectController {

    @Autowired
    private ConnectFeign connectFeign;

    @Operation(summary = "所有协议", description = "")
    @GetMapping("getAll")
    public JsonResponseEntity<List<MapDto>> getAll() {
        List<ConnectDto> connects;
        try {
            connects = RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByProtocol(ProtocolEnum.opc_ua.name()));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        List<MapDto> r = new ArrayList<>();
        if (CollectionUtil.isNotBlank(connects)) {
            for (ConnectDto Connect : connects) {
                r.add(new MapDto().setId(Connect.getServerName()).setName(Connect.getServerName()));
            }
        }
        return JsonResponseEntity.success(r);
    }
}
