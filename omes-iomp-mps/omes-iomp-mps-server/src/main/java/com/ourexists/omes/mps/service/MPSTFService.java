/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.service;

import com.ourexists.era.framework.orm.mybatisplus.service.IMyBatisPlusService;
import com.ourexists.omes.mps.enums.MPSTFStatusEnum;
import com.ourexists.omes.mps.pojo.MPSTF;

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

    /**
     * 未开工 TF（COMMON）强制标 STOP。已 COMPLETE 不动。
     */
    void forceStopUnstartedByMpsId(String mpsId);

    /**
     * 强制中止：COMMON/EXEC → STOP（绕开错误 preCode）。已 COMPLETE 不动。
     * 用于 EXECING 批次业务取消，不触发 COMPLETE→FILE→MpsPush。
     */
    void forceStopByMpsId(String mpsId);
}
