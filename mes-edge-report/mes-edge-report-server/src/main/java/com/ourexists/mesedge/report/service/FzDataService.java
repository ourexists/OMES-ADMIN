/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.FzData;
import com.ourexists.mesedge.report.model.FzDataPageQuery;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface FzDataService extends IMyBatisPlusService<FzData> {

    Page<FzData> selectByPage(FzDataPageQuery dto);

    List<String> allPFName();
}
