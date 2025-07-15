/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mat.pojo.BOMD;

import java.util.List;

public interface BOMDService extends IMyBatisPlusService<BOMD> {

    List<BOMD> selectByMCode(String mcode);

    List<BOMD> selectByMCode(List<String> mcode);

    boolean existMat(List<String> matIds);
}
