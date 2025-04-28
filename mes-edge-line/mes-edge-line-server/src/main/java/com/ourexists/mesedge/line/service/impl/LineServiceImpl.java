/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.line.mapper.LineMapper;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.line.model.TFDto;
import com.ourexists.mesedge.line.model.query.LinePageQuery;
import com.ourexists.mesedge.line.pojo.Line;
import com.ourexists.mesedge.line.pojo.TF;
import com.ourexists.mesedge.line.service.LineService;
import com.ourexists.mesedge.line.service.TFService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class LineServiceImpl extends AbstractMyBatisPlusService<LineMapper, Line> implements LineService {

    @Autowired
    private TFService tfService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetLineTF(ResetLineTFDto dto) {
        Line line = this.selectByCode(dto.getLineCode());
        if (line == null) {
            throw new BusinessException("不存在该生产工艺！");
        }
        for (TFDto tf : dto.getTfs()) {
            tf.setLineId(line.getId());
        }
        tfService.remove(new LambdaQueryWrapper<TF>().eq(TF::getLineId, line.getId()));
        if (tfService.saveBatch(TF.wrap(dto.getTfs()))) {
            this.update(new LambdaUpdateWrapper<Line>()
                    .set(Line::getSyncTime, new Date())
                    .eq(Line::getId, line.getId())
            );
        }
    }


    @Override
    public Page<Line> selectByPage(LinePageQuery dto) {
        LambdaQueryWrapper<Line> qw = new LambdaQueryWrapper<Line>()
                .eq(StringUtils.isNotEmpty(dto.getSelfCode()), Line::getSelfCode, dto.getSelfCode())
                .like(StringUtils.isNotEmpty(dto.getName()), Line::getName, dto.getName())
                .orderByDesc(Line::getId);
        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), qw);
    }

    @Override
    public Line selectByCode(String code) {
        return getOne(new LambdaQueryWrapper<Line>().eq(Line::getSelfCode, code));
    }

    @Override
    public List<Line> selectByCodes(List<String> codes) {
        return list(new LambdaQueryWrapper<Line>().in(Line::getSelfCode, codes));
    }


}
