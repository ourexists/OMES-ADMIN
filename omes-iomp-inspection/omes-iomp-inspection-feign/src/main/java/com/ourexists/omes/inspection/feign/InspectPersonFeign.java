/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectPersonDto;
import com.ourexists.omes.inspection.model.InspectPersonPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface InspectPersonFeign {

    JsonResponseEntity<List<InspectPersonDto>> selectByPage(@RequestBody InspectPersonPageQuery query);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPersonDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectPersonDto> selectById(@RequestParam String id);
}
