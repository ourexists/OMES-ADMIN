/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.sync.enums.ProtocolEnum;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import com.ourexists.mesedge.sync.model.query.ConnectPageQuery;
import com.ourexists.mesedge.sync.pojo.Connect;
import com.ourexists.mesedge.sync.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
public class ConnectViewer implements ConnectFeign {

    @Autowired
    private ConnectService connectService;

    @Override
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<ConnectDto>> selectByPage(@RequestBody ConnectPageQuery query) {
        Page<Connect> page = connectService.selectByPage(query);
        return JsonResponseEntity.success(Connect.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Override
    public JsonResponseEntity<List<ConnectDto>> selectConnectByProtocol(@RequestParam String protocol) {
        List<Connect> connects = connectService.getConnectByProtocol(ProtocolEnum.valueOf(protocol));
        return JsonResponseEntity.success(Connect.covert(connects));
    }

    @Override
    public JsonResponseEntity<ConnectDto> selectConnectByName(@RequestParam String serverName) {
        Connect connect = connectService.getConnect(serverName);
        return JsonResponseEntity.success(Connect.covert(connect));
    }

    @Override
    @GetMapping("selectById")
    public JsonResponseEntity<ConnectDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Connect.covert(connectService.getById(id)));
    }

    @Override
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody ConnectDto dto) {
        connectService.saveOrUpdate(Connect.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Override
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        connectService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @GetMapping("listCollectEnabled")
    public JsonResponseEntity<List<ConnectDto>> listCollectEnabled() {
        List<Connect> list = connectService.listCollectEnabledConnects();
        return JsonResponseEntity.success(Connect.covert(list));
    }
}
