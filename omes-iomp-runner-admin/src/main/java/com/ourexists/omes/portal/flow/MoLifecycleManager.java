/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.flow;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.mo.enums.MOStatusEnum;
import com.ourexists.omes.mo.feign.MOFeign;
import com.ourexists.omes.mo.model.MODto;
import com.ourexists.omes.mps.enums.MPSStatusEnum;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.mps.model.MPSDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MO 生命周期闭环：全部子 MPS 终态后驱动 COMPLETE / CANCEL。
 */
@Slf4j
@Component
public class MoLifecycleManager {

    @Autowired
    private MOFeign moFeign;

    @Autowired
    private MPSFeign mpsFeign;

    @Transactional(rollbackFor = Exception.class)
    public void recalc(String moCode) {
        if (StringUtils.isBlank(moCode)) {
            return;
        }
        MODto mo;
        List<MPSDto> mpsList;
        try {
            mo = RemoteHandleUtils.getDataFormResponse(moFeign.selectByCode(moCode));
            mpsList = RemoteHandleUtils.getDataFormResponse(mpsFeign.selectByMoCode(moCode));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (mo == null) {
            return;
        }
        if (CollectionUtil.isBlank(mpsList)) {
            // 无子计划：保持 INIT/CANCEL；不强制 COMPLETE
            return;
        }
        boolean hasOpen = false;
        boolean hasDone = false;
        boolean allCancel = true;
        for (MPSDto mps : mpsList) {
            Integer st = mps.getStatus();
            if (st == null) {
                continue;
            }
            if (MPSStatusEnum.WAIT_QUE.getCode().equals(st)
                    || MPSStatusEnum.WAIT_EXEC.getCode().equals(st)
                    || MPSStatusEnum.EXECING.getCode().equals(st)) {
                hasOpen = true;
                allCancel = false;
            } else if (MPSStatusEnum.COMPLETE.getCode().equals(st)
                    || MPSStatusEnum.FILE.getCode().equals(st)) {
                hasDone = true;
                allCancel = false;
            } else if (!MPSStatusEnum.CANCEL.getCode().equals(st)) {
                allCancel = false;
            }
        }
        try {
            if (!hasOpen && hasDone) {
                if (!MOStatusEnum.COMPLETE.getCode().equals(mo.getStatus())
                        && !MOStatusEnum.CANCEL.getCode().equals(mo.getStatus())) {
                    RemoteHandleUtils.getDataFormResponse(
                            moFeign.updateStatus(java.util.Collections.singletonList(moCode), MOStatusEnum.COMPLETE));
                    log.info("MO lifecycle → COMPLETE mo={}", moCode);
                }
            } else if (allCancel || (!hasOpen && !hasDone)) {
                if (!MOStatusEnum.CANCEL.getCode().equals(mo.getStatus())) {
                    RemoteHandleUtils.getDataFormResponse(
                            moFeign.updateStatus(java.util.Collections.singletonList(moCode), MOStatusEnum.CANCEL));
                    log.info("MO lifecycle → CANCEL mo={}", moCode);
                }
            }
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
