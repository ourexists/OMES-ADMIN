package com.ourexists.omes.inspection.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.model.InspectRecordDto;
import com.ourexists.omes.inspection.model.InspectRecordListByEquipPeriodQuery;
import com.ourexists.omes.inspection.model.InspectRecordPageQuery;
import com.ourexists.omes.inspection.model.InspectRecordSaveRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface InspectRecordFeign {

    JsonResponseEntity<List<InspectRecordDto>> selectByPage(@RequestBody InspectRecordPageQuery query);

    /**
     * 按设备ID与统计周期查询巡检记录（用于设备健康分巡检维度）
     */
    JsonResponseEntity<List<InspectRecordDto>> listByEquipIdAndPeriod(@RequestBody InspectRecordListByEquipPeriodQuery query);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectRecordDto dto);

    JsonResponseEntity<Boolean> save(@Validated @RequestBody InspectRecordSaveRequest request);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<InspectRecordDto> selectById(@RequestParam String id);

    JsonResponseEntity<List<InspectRecordDto>> listByTaskId(@RequestParam String taskId);
}
