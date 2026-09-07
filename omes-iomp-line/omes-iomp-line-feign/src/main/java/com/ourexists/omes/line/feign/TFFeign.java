/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.line.model.TFDto;
import com.ourexists.omes.line.model.TFVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TFFeign {

    JsonResponseEntity<List<TFVo>> selectByLineId(@RequestParam String lineId);

    JsonResponseEntity<TFVo> selectById(@RequestParam String id);

    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TFDto dto);

    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
