package com.ourexists.mesedge.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.feign.PlatformFeign;
import com.ourexists.mesedge.ucenter.platform.PlatformDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "平台")
@RestController
@RequestMapping("/platform")
public class PlatformController {

    @Autowired
    private PlatformFeign platformFeign;

    @Operation(summary = "分页查询")
    @GetMapping("/getAll")
    public JsonResponseEntity<List<PlatformDto>> getAll() {
        return platformFeign.getAll();
    }

    @Operation(summary = "新增更新")
    @PostMapping("/addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated PlatformDto dto) {
        return platformFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return platformFeign.delete(idsDto);
    }
}
