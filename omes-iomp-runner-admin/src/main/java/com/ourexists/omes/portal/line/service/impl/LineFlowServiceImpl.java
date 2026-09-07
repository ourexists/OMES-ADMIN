/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.line.service.impl;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.GatewayFeign;
import com.ourexists.omes.device.model.GatewayDto;
import com.ourexists.omes.line.feign.LineFeign;
import com.ourexists.omes.line.model.ResetLineTFDto;
import com.ourexists.omes.mps.feign.MPSFeign;
import com.ourexists.omes.portal.line.service.LineFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LineFlowServiceImpl implements LineFlowService {

    @Autowired
    private MPSFeign mpsFeign;

    @Autowired
    private GatewayFeign gatewayFeign;

    @Autowired
    private LineFeign lineFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetLineTF(ResetLineTFDto dto) {
        try {
            Long count = RemoteHandleUtils.getDataFormResponse(mpsFeign.countExecByCode(dto.getLineCode()));
            if (count > 0) {
                throw new BusinessException("当前工艺已存在待生产计划，无法更新!");
            }
            RemoteHandleUtils.getDataFormResponse(lineFeign.resetLineTF(dto));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void downloadS7(String lineId, String serverName) {
        try {
            GatewayDto connect = RemoteHandleUtils.getDataFormResponse(gatewayFeign.selectConnectByName(serverName));
            if (connect == null) {
                throw new BusinessException("PLC 网关不存在: " + serverName);
            }
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        // S7/OPC UA 下发逻辑待接入规则引擎参数后恢复
        log.debug("downloadS7 skipped: lineId={}, serverName={}", lineId, serverName);
    }
}
