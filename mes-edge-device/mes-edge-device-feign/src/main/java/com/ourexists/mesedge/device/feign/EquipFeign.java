package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface EquipFeign {

    JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EquipDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<EquipDto> selectById(@RequestParam String id,
                                            @RequestParam Boolean needRealtime);
}
