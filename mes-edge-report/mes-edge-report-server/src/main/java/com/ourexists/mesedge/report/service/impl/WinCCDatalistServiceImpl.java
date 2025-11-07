/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.mesedge.report.mapper.WinCCDatalistMapper;
import com.ourexists.mesedge.report.model.WinCCDatalist;
import com.ourexists.mesedge.report.model.WinCCDatalistPageQuery;
import com.ourexists.mesedge.report.service.WinCCDatalistService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WinCCDatalistServiceImpl extends AbstractMyBatisPlusService<WinCCDatalistMapper, WinCCDatalist>
        implements WinCCDatalistService {

    @Override
    public Page<WinCCDatalist> selectByPage(WinCCDatalistPageQuery dto) {
        List<WinCCDatalist> winCCDatalistList = this.baseMapper.page(dto);
        int size = 0;
        if (CollectionUtil.isNotBlank(winCCDatalistList)) {
            size = winCCDatalistList.size();
        }
        Page<WinCCDatalist> page = new Page<>(dto.getPage(), dto.getPageSize());
        page.setRecords(winCCDatalistList);
        page.setTotal(size);
        return page;
    }

}
