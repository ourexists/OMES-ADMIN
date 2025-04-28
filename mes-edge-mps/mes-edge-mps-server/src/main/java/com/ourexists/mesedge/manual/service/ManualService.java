/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.manual.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.manual.pojo.Manual;
import com.ourexists.mesedge.mps.model.QACheckDto;
import com.ourexists.mesedge.mps.model.QAPageQuery;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface ManualService extends IMyBatisPlusService<Manual> {

    List<Manual> selectByMpsId(String mspId);

    Page<Manual> selectByPage(QAPageQuery dto);

    void check(QACheckDto dto);
}
