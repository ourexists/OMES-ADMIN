/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.mat.mapper.BOMDMapper;
import com.ourexists.mesedge.mat.pojo.BOMD;
import com.ourexists.mesedge.mat.service.BOMDService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BOMDServiceImpl extends AbstractMyBatisPlusService<BOMDMapper, BOMD>
        implements BOMDService {

    @Override
    public List<BOMD> selectByMCode(String mcode) {
        return this.list(
                new LambdaQueryWrapper<BOMD>()
                        .eq(BOMD::getMcode, mcode));
    }

    @Override
    public List<BOMD> selectByMCode(List<String> mcode) {
        if (CollectionUtil.isBlank(mcode)) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<BOMD>()
                        .in(BOMD::getMcode, mcode));
    }

}
