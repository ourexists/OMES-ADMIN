/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.line.mapper.TFEdgeMapper;
import com.ourexists.omes.line.model.TFEdgeDto;
import com.ourexists.omes.line.model.TFEdgeSaveDto;
import com.ourexists.omes.line.pojo.TFEdge;
import com.ourexists.omes.line.service.TFEdgeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TFEdgeServiceImpl extends AbstractMyBatisPlusService<TFEdgeMapper, TFEdge> implements TFEdgeService {

    @Override
    public List<TFEdge> selectByLineId(String lineId) {
        return this.list(new LambdaQueryWrapper<TFEdge>().eq(TFEdge::getLineId, lineId));
    }

    @Override
    public void saveByLineId(TFEdgeSaveDto dto) {
        if (dto == null || StringUtils.isBlank(dto.getLineId())) {
            return;
        }

        // 以 lineId 为粒度，保存整张关系图：先清空再批量插入
        this.remove(new LambdaQueryWrapper<TFEdge>().eq(TFEdge::getLineId, dto.getLineId()));

        if (dto.getEdges() == null || dto.getEdges().isEmpty()) {
            return;
        }

        List<TFEdge> edges = new ArrayList<>();
        for (TFEdgeDto edge : dto.getEdges()) {
            if (edge == null) {
                continue;
            }
            if (StringUtils.isBlank(edge.getFromTfId()) || StringUtils.isBlank(edge.getToTfId())) {
                continue;
            }
            TFEdge tfEdge = TFEdge.wrap(edge);
            tfEdge.setLineId(dto.getLineId());
            edges.add(tfEdge);
        }

        if (!edges.isEmpty()) {
            this.saveBatch(edges);
        }
    }
}

