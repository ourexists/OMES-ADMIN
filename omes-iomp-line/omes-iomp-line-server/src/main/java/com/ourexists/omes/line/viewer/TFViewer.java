/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.viewer;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.line.feign.TFFeign;
import com.ourexists.omes.line.model.TFDto;
import com.ourexists.omes.line.model.TFVo;
import com.ourexists.omes.line.pojo.TF;
import com.ourexists.omes.line.service.TFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class TFViewer implements TFFeign {

    @Autowired
    private TFService service;

    public JsonResponseEntity<List<TFVo>> selectByLineId(@RequestParam String lineId) {
        return JsonResponseEntity.success(TF.covert(service.selectByLineId(lineId)));
    }

    public JsonResponseEntity<TFVo> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(TF.covert(service.getById(id)));
    }

    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TFDto dto) {
        service.saveOrUpdate(TF.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
