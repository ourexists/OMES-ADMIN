/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectItemDto;
import com.ourexists.omes.inspection.model.InspectItemPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 巡检项 Feign 接口
 */
public interface InspectItemFeign {

    JsonResponseEntity<List<InspectItemDto>> selectByPage(@RequestBody InspectItemPageQuery query);

    JsonResponseEntity<List<InspectItemDto>> listAllPool();

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectItemDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectItemDto> selectById(@RequestParam String id);
}
