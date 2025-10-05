/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.report.model.LmRecord;
import com.ourexists.mesedge.report.model.LmRecordPageQuery;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface LmRecordService extends IMyBatisPlusService<LmRecord> {

    Page<LmRecord> selectByPage(LmRecordPageQuery dto);

    Long selectSumll(Integer fzId);

    Long selectSumSj(Integer fzId);
}
