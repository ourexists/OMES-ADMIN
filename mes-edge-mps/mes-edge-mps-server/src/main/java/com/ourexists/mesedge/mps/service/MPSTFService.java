/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import com.ourexists.mesedge.mps.pojo.MPSTF;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/2 16:19
 * @since 1.0.0
 */
public interface MPSTFService extends IMyBatisPlusService<MPSTF> {

    MPSTF selectById(String id);

    List<MPSTF> selectByIds(List<String> ids);

    List<MPSTF> selectByMPSId(String mpsId);

    List<MPSTF> selectByMPSId(List<String> mids);

    void updateStatus(String mpstfId, MPSTFStatusEnum mpstfStatus);
}
