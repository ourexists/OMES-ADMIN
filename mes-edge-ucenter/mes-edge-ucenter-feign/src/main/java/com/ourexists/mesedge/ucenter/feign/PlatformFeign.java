package com.ourexists.mesedge.ucenter.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ucenter.platform.PlatformDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PlatformFeign {

    JsonResponseEntity<List<PlatformDto>> getAll();

    JsonResponseEntity<Boolean> addOrUpdate(@RequestBody @Validated PlatformDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
