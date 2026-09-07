/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.service;

import com.ourexists.omes.mps.pojo.MPSDetail;
import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;

import java.util.List;

public interface MPSDetailService extends IMyBatisPlusService<MPSDetail> {

    List<MPSDetail> selectByMid(String mid);

    List<MPSDetail> selectByMid(List<String> mids);
}
