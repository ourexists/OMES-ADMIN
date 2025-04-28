/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.mesedge.mps.mapper.MPSDetailMapper;
import com.ourexists.mesedge.mps.pojo.MPSDetail;
import com.ourexists.mesedge.mps.service.MPSDetailService;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MPSDetailServiceImpl extends AbstractMyBatisPlusService<MPSDetailMapper, MPSDetail>
        implements MPSDetailService {

    @Override
    public List<MPSDetail> selectByMid(String mid) {
        return this.list(
                new LambdaQueryWrapper<MPSDetail>()
                        .eq(MPSDetail::getMid, mid));
    }

    @Override
    public List<MPSDetail> selectByMid(List<String> mids) {
        if (CollectionUtil.isBlank(mids)) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<MPSDetail>()
                        .in(MPSDetail::getMid, mids));
    }
}
