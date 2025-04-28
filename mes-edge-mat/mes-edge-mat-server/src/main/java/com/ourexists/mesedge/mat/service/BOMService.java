/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mat.model.BOMDto;
import com.ourexists.mesedge.mat.pojo.BOM;
import com.ourexists.mesedge.mat.model.query.BOMPageQuery;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface BOMService extends IMyBatisPlusService<BOM> {

    Page<BOM> selectByPage(BOMPageQuery dto);

    void addOrUpdate(BOMDto dto);
}
