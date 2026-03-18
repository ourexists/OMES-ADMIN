/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectTemplateDto;
import com.ourexists.omes.inspection.model.InspectTemplatePageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 巡检模板 Feign 接口
 */
public interface InspectTemplateFeign {

    JsonResponseEntity<List<InspectTemplateDto>> selectByPage(@RequestBody InspectTemplatePageQuery query);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTemplateDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectTemplateDto> selectById(@RequestParam String id);

    /** 根据模板ID查询巡检项列表（含模板信息） */
    JsonResponseEntity<InspectTemplateDto> selectWithItems(@RequestParam String templateId);

    /** 模板列表（下拉等） */
    JsonResponseEntity<List<InspectTemplateDto>> selectList();
}
