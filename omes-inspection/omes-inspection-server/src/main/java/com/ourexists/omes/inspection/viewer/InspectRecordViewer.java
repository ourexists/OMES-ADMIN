/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectRecordFeign;
import com.ourexists.omes.inspection.model.InspectRecordDto;
import com.ourexists.omes.inspection.model.InspectRecordItemDto;
import com.ourexists.omes.inspection.model.InspectRecordPageQuery;
import com.ourexists.omes.inspection.model.InspectRecordSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.omes.inspection.pojo.InspectRecord;
import com.ourexists.omes.inspection.pojo.InspectRecordItem;
import com.ourexists.omes.inspection.service.InspectRecordItemService;
import com.ourexists.omes.inspection.service.InspectRecordService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InspectRecordViewer implements InspectRecordFeign {

    @Autowired
    private InspectRecordService service;
    @Autowired
    private InspectRecordItemService inspectRecordItemService;

    @Override
    @Operation(summary = "分页查询巡检记录")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectRecordDto>> selectByPage(@RequestBody InspectRecordPageQuery query) {
        Page<InspectRecord> page = service.selectByPage(query);
        List<InspectRecordDto> list = InspectRecord.covert(page.getRecords());
        fillItems(list);
        return JsonResponseEntity.success(list, OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "新增或修改巡检记录")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectRecordDto dto) {
        InspectRecord main = InspectRecord.wrap(dto);
        service.saveOrUpdate(main);
        String recordId = main.getId();
        if (recordId != null && dto.getItems() != null && !dto.getItems().isEmpty()) {
            inspectRecordItemService.remove(new LambdaQueryWrapper<InspectRecordItem>().eq(InspectRecordItem::getRecordId, recordId));
            List<InspectRecordItem> details = InspectRecordItem.wrap(dto.getItems());
            details.forEach(e -> e.setRecordId(recordId));
            inspectRecordItemService.saveBatch(details);
        }
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "生成巡检记录（APP 提交）")
    @PostMapping("save")
    public JsonResponseEntity<Boolean> save(@Validated @RequestBody InspectRecordSaveRequest request) {
        service.saveRecord(request);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检记录")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        if (idsDto.getIds() != null && !idsDto.getIds().isEmpty()) {
            inspectRecordItemService.remove(new LambdaQueryWrapper<InspectRecordItem>().in(InspectRecordItem::getRecordId, idsDto.getIds()));
        }
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检记录")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectRecordDto> selectById(@RequestParam String id) {
        InspectRecord record = service.getById(id);
        InspectRecordDto dto = InspectRecord.covert(record);
        if (dto != null) {
            fillItems(Collections.singletonList(dto));
        }
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "按任务ID查询记录列表")
    @GetMapping("listByTaskId")
    public JsonResponseEntity<List<InspectRecordDto>> listByTaskId(@RequestParam String taskId) {
        List<InspectRecord> records = service.listByTaskId(taskId);
        List<InspectRecordDto> list = InspectRecord.covert(records);
        fillItems(list);
        return JsonResponseEntity.success(list);
    }

    /** 为主表 DTO 列表填充附表 items */
    private void fillItems(List<InspectRecordDto> list) {
        if (list == null || list.isEmpty()) return;
        List<String> recordIds = list.stream().map(InspectRecordDto::getId).filter(id -> id != null && !id.isEmpty()).collect(Collectors.toList());
        if (recordIds.isEmpty()) return;
        List<InspectRecordItem> items = inspectRecordItemService.listByRecordIds(recordIds);
        java.util.Map<String, List<InspectRecordItemDto>> map = items.stream()
                .collect(Collectors.groupingBy(InspectRecordItem::getRecordId,
                        Collectors.mapping(InspectRecordItem::covert, Collectors.toList())));
        for (InspectRecordDto dto : list) {
            if (dto.getId() != null) {
                dto.setItems(map.getOrDefault(dto.getId(), Collections.emptyList()));
            }
        }
    }
}
