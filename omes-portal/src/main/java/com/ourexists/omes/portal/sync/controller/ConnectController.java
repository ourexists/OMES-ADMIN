/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.sync.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.sync.enums.ProtocolEnum;
import com.ourexists.omes.sync.feign.ConnectFeign;
import com.ourexists.omes.sync.model.ConnectDto;
import com.ourexists.omes.sync.model.query.ConnectPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Tag(name = "连接管理")
@RestController
@RequestMapping("/connect")
public class ConnectController {

    @Autowired
    private ConnectFeign connectFeign;


    @Operation(summary = "分页查询", description = "连接分页列表")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<ConnectDto>> selectByPage(@RequestBody ConnectPageQuery query) {
        return connectFeign.selectByPage(query);
    }

    @Operation(summary = "按ID查询", description = "单条连接详情")
    @GetMapping("selectById")
    public JsonResponseEntity<ConnectDto> selectById(@RequestParam String id) {
        return connectFeign.selectById(id);
    }

    @Operation(summary = "新增或修改", description = "根据id判断新增或更新")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ConnectDto dto) {
        return connectFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "按id列表删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return connectFeign.delete(idsDto);
    }

    @Operation(summary = "协议枚举列表", description = "返回所有支持的协议类型，用于下拉/配置")
    @GetMapping("protocols")
    public JsonResponseEntity<List<MapDto>> getProtocols() {
        List<MapDto> list = Arrays.stream(ProtocolEnum.values())
                .map(p -> new MapDto().setId(p.name()).setName(p.getDesc()))
                .collect(Collectors.toList());
        return JsonResponseEntity.success(list);
    }

    @Operation(summary = "按协议查询连接列表", description = "返回指定协议下的所有连接，protocol 取 protocols 接口的 id")
    @GetMapping("getByProtocol")
    public JsonResponseEntity<List<ConnectDto>> getByProtocol(@RequestParam String protocol) {
        ProtocolEnum protocolEnum;
        try {
            protocolEnum = ProtocolEnum.valueOf(protocol);
        } catch (IllegalArgumentException e) {
            return JsonResponseEntity.success(new ArrayList<>());
        }
        try {
            List<ConnectDto> connects = RemoteHandleUtils.getDataFormResponse(
                    connectFeign.selectConnectByProtocol(protocolEnum.name()));
            return JsonResponseEntity.success(connects != null ? connects : new ArrayList<>());
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "OPC UA 连接列表（兼容）", description = "返回 OPC UA 协议下的连接，用于下拉选择服务名")
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
            for (ConnectDto connect : connects) {
                r.add(new MapDto().setId(connect.getServerName()).setName(connect.getServerName()));
            }
        }
        return JsonResponseEntity.success(r);
    }
}
