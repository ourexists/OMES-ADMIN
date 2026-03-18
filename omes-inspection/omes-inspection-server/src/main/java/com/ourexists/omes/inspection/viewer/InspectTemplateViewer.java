/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectTemplateFeign;
import com.ourexists.omes.inspection.model.InspectTemplateDto;
import com.ourexists.omes.inspection.model.InspectTemplateItemDto;
import com.ourexists.omes.inspection.model.InspectTemplatePageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.omes.inspection.pojo.InspectItem;
import com.ourexists.omes.inspection.pojo.InspectTemplate;
import com.ourexists.omes.inspection.pojo.InspectTemplateItem;
import com.ourexists.omes.inspection.service.InspectItemService;
import com.ourexists.omes.inspection.service.InspectTemplateItemService;
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
public class InspectTemplateViewer implements InspectTemplateFeign {

    @Autowired
    private InspectTemplateService templateService;
    @Autowired
    private InspectTemplateItemService templateItemService;
    @Autowired
    private InspectItemService inspectItemService;

    @Override
    @Operation(summary = "分页查询巡检模板")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectTemplateDto>> selectByPage(@RequestBody InspectTemplatePageQuery query) {
        Page<InspectTemplate> page = templateService.selectByPage(query);
        return JsonResponseEntity.success(InspectTemplate.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "新增或修改巡检模板（可带巡检项列表）")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTemplateDto dto) {
        InspectTemplate template = InspectTemplate.wrap(dto);
        templateService.saveOrUpdate(template);
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<InspectTemplateItem> items = InspectTemplateItem.wrap(dto.getItems());
            templateItemService.saveBatch(template.getId(), items);
        } else {
            // 没有传巡检项则清空该模板下的配置
            templateItemService.deleteByTemplateId(template.getId());
        }
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检模板")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        for (String id : idsDto.getIds()) {
            templateItemService.deleteByTemplateId(id);
        }
        templateService.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检模板")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectTemplateDto> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(InspectTemplate.covert(templateService.getById(id)));
    }

    @Override
    @Operation(summary = "查询模板及其巡检项列表")
    @GetMapping("selectWithItems")
    public JsonResponseEntity<InspectTemplateDto> selectWithItems(@RequestParam String templateId) {
        InspectTemplate template = templateService.getById(templateId);
        if (template == null) return JsonResponseEntity.success(null);
        InspectTemplateDto dto = InspectTemplate.covert(template);
        List<InspectTemplateItem> items = templateItemService.listByTemplateId(templateId);
        List<InspectTemplateItemDto> itemDtos = InspectTemplateItem.covert(items);
        // 通过引用的巡检项ID回填名称、类型、单位（仅返回，不落表）
        List<String> refIds = items.stream()
                .map(InspectTemplateItem::getReferenceItemId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (!refIds.isEmpty()) {
            Map<String, InspectItem> idToItem = inspectItemService.listByIds(refIds).stream()
                    .collect(Collectors.toMap(InspectItem::getId, i -> i, (a, b) -> a));
            for (InspectTemplateItemDto it : itemDtos) {
                String refId = it.getReferenceItemId();
                if (refId != null && !refId.isBlank()) {
                    InspectItem ref = idToItem.get(refId);
                    if (ref != null) {
                        it.setItemName(ref.getItemName());
                        it.setItemType(ref.getItemType());
                        it.setUnit(ref.getUnit());
                    }
                }
            }
        }
        dto.setItems(itemDtos);
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "模板列表（下拉等）")
    @GetMapping("selectList")
    public JsonResponseEntity<List<InspectTemplateDto>> selectList() {
        return JsonResponseEntity.success(InspectTemplate.covert(templateService.selectList()));
    }
}
