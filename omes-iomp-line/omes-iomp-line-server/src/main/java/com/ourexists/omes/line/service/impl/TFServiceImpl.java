/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.line.mapper.TFMapper;
import com.ourexists.omes.line.pojo.TF;
import com.ourexists.omes.line.service.TFService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TFServiceImpl extends AbstractMyBatisPlusService<TFMapper, TF> implements TFService {

    @Override
    public List<TF> selectByLineId(String lineId) {
        return this.list(new LambdaQueryWrapper<TF>()
                .eq(TF::getLineId, lineId)
                .orderByAsc(TF::getStepNo)
                .orderByAsc(TF::getId));
    }
}
