/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectItemFeign;
import com.ourexists.omes.inspection.model.InspectItemDto;
import com.ourexists.omes.inspection.model.InspectItemPageQuery;
import com.ourexists.omes.inspection.pojo.InspectItem;
import com.ourexists.omes.inspection.pojo.InspectTemplate;
import com.ourexists.omes.inspection.service.InspectItemService;
import com.ourexists.omes.inspection.service.InspectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InspectItemViewer implements InspectItemFeign {

    @Autowired
    private InspectItemService service;
    @Autowired
    private InspectTemplateService templateService;

    @Override
    @Operation(summary = "分页查询巡检项")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectItemDto>> selectByPage(@RequestBody InspectItemPageQuery query) {
        Page<InspectItem> page = service.selectByPage(query);
        List<InspectItemDto> list = InspectItem.covert(page.getRecords());
        fillTemplateNames(list);
        return JsonResponseEntity.success(list, OrmUtils.extraPagination(page));
    }

    private void fillTemplateNames(List<InspectItemDto> list) {
        if (list == null || list.isEmpty()) return;
        List<String> templateIds = list.stream()
                .map(InspectItemDto::getTemplateId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (templateIds.isEmpty()) return;
        Map<String, String> idToName = templateService.listByIds(templateIds).stream()
                .collect(Collectors.toMap(InspectTemplate::getId, t -> t.getName() != null ? t.getName() : "", (a, b) -> a));
        for (InspectItemDto dto : list) {
            if (dto.getTemplateId() != null) {
                dto.setTemplateName(idToName.getOrDefault(dto.getTemplateId(), ""));
            }
        }
    }


    @Override
    @Operation(summary = "公共库巡检项列表（未绑定模板，供模板「从巡检项载入」使用）")
    @GetMapping("listAllPool")
    public JsonResponseEntity<List<InspectItemDto>> listAllPool() {
        return JsonResponseEntity.success(InspectItem.covert(service.listAllPool()));
    }

    @Override
    @Operation(summary = "新增或修改单条巡检项")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectItemDto dto) {
        // templateId 可为空，为空时为公共库巡检项（供模板载入使用）
        InspectItem item = InspectItem.wrap(dto);
        service.saveOrUpdate(item);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检项")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检项")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectItemDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(InspectItem.covert(service.getById(id)));
    }
}
