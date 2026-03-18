package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectPlanDto;
import com.ourexists.omes.inspection.model.InspectPlanPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface InspectPlanFeign {

    JsonResponseEntity<List<InspectPlanDto>> selectByPage(@RequestBody InspectPlanPageQuery query);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPlanDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectPlanDto> selectById(@RequestParam String id);

    JsonResponseEntity<Boolean> enable(@RequestParam String planId);

    JsonResponseEntity<Boolean> disable(@RequestParam String planId);

    JsonResponseEntity<Boolean> generateTasks(@RequestParam String planId);
}
