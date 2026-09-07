/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mo.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.mo.pojo.MoAdjustLog;

import java.util.List;

public interface MoAdjustLogService extends IMyBatisPlusService<MoAdjustLog> {

    MoAdjustLog selectByRequestId(String requestId);

    List<MoAdjustLog> selectByMoCode(String moCode);
}
