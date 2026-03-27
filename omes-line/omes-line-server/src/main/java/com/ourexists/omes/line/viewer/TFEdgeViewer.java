/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.line.feign.TFEdgeFeign;
import com.ourexists.omes.line.model.TFEdgeSaveDto;
import com.ourexists.omes.line.model.TFEdgeVo;
import com.ourexists.omes.line.pojo.TFEdge;
import com.ourexists.omes.line.service.TFEdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class TFEdgeViewer implements TFEdgeFeign {

    @Autowired
    private TFEdgeService service;

    @Override
    public JsonResponseEntity<List<TFEdgeVo>> selectByLineId(@RequestParam String lineId) {
        List<TFEdge> edges = service.selectByLineId(lineId);
        return JsonResponseEntity.success(TFEdge.covert(edges));
    }

    @Override
    public JsonResponseEntity<Boolean> saveByLineId(@Validated @RequestBody TFEdgeSaveDto dto) {
        service.saveByLineId(dto);
        return JsonResponseEntity.success(true);
    }
}

