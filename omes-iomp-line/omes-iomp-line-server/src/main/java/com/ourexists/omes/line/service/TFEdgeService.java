/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.line.model.TFEdgeSaveDto;
import com.ourexists.omes.line.pojo.TFEdge;

import java.util.List;

public interface TFEdgeService extends IMyBatisPlusService<TFEdge> {

    List<TFEdge> selectByLineId(String lineId);

    void saveByLineId(TFEdgeSaveDto dto);
}

