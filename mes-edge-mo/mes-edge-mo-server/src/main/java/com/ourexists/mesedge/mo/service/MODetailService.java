/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mo.pojo.MODetail;
import com.ourexists.mesedge.mo.model.query.MODetailPageQuery;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface MODetailService extends IMyBatisPlusService<MODetail> {

    Page<MODetail> selectByPage(MODetailPageQuery dto);

    List<MODetail> selectByMcode(String mcode);
}
