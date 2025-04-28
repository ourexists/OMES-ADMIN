/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.line.mapper.TFMapper;
import com.ourexists.mesedge.line.model.ChangePriorityDto;
import com.ourexists.mesedge.line.pojo.TF;
import com.ourexists.mesedge.line.service.TFService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TFServiceImpl extends AbstractMyBatisPlusService<TFMapper, TF> implements TFService {

    @Override
    public List<TF> selectByLineId(String lineId) {
        return this.list(new LambdaQueryWrapper<TF>().eq(TF::getLineId, lineId).orderByAsc(TF::getPriority));
    }

    @Override
    public void changePriority(ChangePriorityDto dto) {
        List<String> allIds = new ArrayList<>();

        if (StringUtils.isNotBlank(dto.getPre())) {
            allIds.add(dto.getPre());
        }
        if (StringUtils.isNotBlank(dto.getPost())) {
            allIds.add(dto.getPost());
        }
        List<TF> tfs = this.listByIds(allIds);
        BigDecimal newPriority = BigDecimal.ZERO;
        for (TF tf : tfs) {
            newPriority = newPriority.add(tf.getPriority());
        }
        if (StringUtils.isBlank(dto.getPost())) {
            newPriority = newPriority.add(BigDecimal.ONE);
        } else {
            newPriority = newPriority.divide(BigDecimal.valueOf(2), 5, RoundingMode.HALF_UP);
        }
        this.update(new LambdaUpdateWrapper<TF>().set(TF::getPriority, newPriority).eq(TF::getId, dto.getCurrent()));
    }
}
