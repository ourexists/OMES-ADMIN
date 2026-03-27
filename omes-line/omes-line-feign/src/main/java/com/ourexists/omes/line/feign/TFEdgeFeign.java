/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.line.model.TFEdgeSaveDto;
import com.ourexists.omes.line.model.TFEdgeVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TFEdgeFeign {

    JsonResponseEntity<List<TFEdgeVo>> selectByLineId(@RequestParam String lineId);

    JsonResponseEntity<Boolean> saveByLineId(@Validated @RequestBody TFEdgeSaveDto dto);
}

