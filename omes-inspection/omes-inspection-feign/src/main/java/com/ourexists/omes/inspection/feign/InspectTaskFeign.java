package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectTaskDto;
import com.ourexists.omes.inspection.model.InspectTaskPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface InspectTaskFeign {

    JsonResponseEntity<List<InspectTaskDto>> selectByPage(@RequestBody InspectTaskPageQuery query);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTaskDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectTaskDto> selectById(@RequestParam String id);

    JsonResponseEntity<List<InspectTaskDto>> listByPlanId(@RequestParam String planId);

    /** 将已逾期的任务重置为待执行 */
    JsonResponseEntity<Boolean> restartOverdue(@RequestBody IdsDto idsDto);
}
