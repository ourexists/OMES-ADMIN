/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.viewer;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.feign.PlatformFeign;
import com.ourexists.mesedge.ucenter.platform.PlatformDto;
import com.ourexists.mesedge.ucenter.platform.pojo.Platform;
import com.ourexists.mesedge.ucenter.platform.service.PlatformService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:22
 * @since 1.0.0
 */
//@Slf4j
//@Tag(name = "角色")
//@RestController
//@RequestMapping("/role")
@Component
public class PlatformViewer implements PlatformFeign {

    @Autowired
    private PlatformService service;

    @Operation(summary = "分页查询")
    @GetMapping("/getAll")
    public JsonResponseEntity<List<PlatformDto>> getAll() {
        List<Platform> platforms = service.list();
        return JsonResponseEntity.success(Platform.covert(platforms));
    }

    @Operation(summary = "新增更新")
    @PostMapping("/addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated PlatformDto dto) {
        service.saveOrUpdate(Platform.warp(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
