/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectPersonFeign;
import com.ourexists.omes.inspection.model.InspectPersonDto;
import com.ourexists.omes.inspection.model.InspectPersonPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPerson;
import com.ourexists.omes.inspection.service.InspectPersonService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class InspectPersonViewer implements InspectPersonFeign {

    @Autowired
    private InspectPersonService service;

    @Override
    @Operation(summary = "分页查询巡检人员")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectPersonDto>> selectByPage(@RequestBody InspectPersonPageQuery query) {
        Page<InspectPerson> page = service.selectByPage(query);
        return JsonResponseEntity.success(InspectPerson.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "新增或修改巡检人员")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPersonDto dto) {
        service.saveOrUpdate(InspectPerson.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检人员")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检人员")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectPersonDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(InspectPerson.covert(service.getById(id)));
    }
}
