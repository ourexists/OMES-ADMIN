/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.manual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.manual.mapper.ManualMapper;
import com.ourexists.mesedge.manual.pojo.Manual;
import com.ourexists.mesedge.manual.service.ManualService;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.model.QACheckDto;
import com.ourexists.mesedge.mps.model.QAPageQuery;
import com.ourexists.mesedge.mps.service.MPSTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManualServiceImpl extends AbstractMyBatisPlusService<ManualMapper, Manual> implements ManualService {

    @Autowired
    private MPSTFService mpstfService;

    @Override
    public List<Manual> selectByMpsId(String mspId) {
        return this.list(new LambdaQueryWrapper<Manual>().eq(Manual::getMpsId, mspId).orderByAsc(Manual::getId));
    }

    @Override
    public Page<Manual> selectByPage(QAPageQuery dto) {
        Long count = this.baseMapper.countPage(dto);
        List<Manual> manualList = null;
        if (count > 0) {
            manualList = this.baseMapper.page(dto);
        }
        Page<Manual> page = new Page<>(dto.getPage(), dto.getPageSize());
        page.setTotal(count);
        page.setRecords(manualList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void check(QACheckDto dto) {
        Manual manual = this.getById(dto.getId());
        if (manual == null) {
            return;
        }
        if (this.update(new LambdaUpdateWrapper<Manual>()
                .set(Manual::getResult, dto.getResult())
                .set(Manual::getMsg, dto.getMsg())
                .set(Manual::getPass, dto.getPass())
                .eq(Manual::getId, dto.getId())
        )) {
            mpstfService.updateStatus(manual.getMpsTfId(), MPSTFStatusEnum.COMPLETE);
        }
    }
}
