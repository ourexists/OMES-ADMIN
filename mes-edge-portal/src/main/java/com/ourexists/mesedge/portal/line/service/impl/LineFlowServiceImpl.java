/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.line.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.line.model.ResetLineTFDto;
import com.ourexists.mesedge.mps.feign.MPSFeign;
import com.ourexists.mesedge.portal.line.service.LineFlowService;
import com.ourexists.mesedge.portal.sync.manager.push.LinePushSyncManager;
import com.ourexists.mesedge.portal.sync.ocpua.OpcUaContext;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
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
    private OpcUaContext opcUaContext;

    @Autowired
    private ConnectFeign connectFeign;

    @Autowired
    private LinePushSyncManager linePushSyncManager;

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
        ConnectDto connect = null;
        try {
            connect = RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(serverName));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (connect == null) {
            return;
        }
        try {
            opcUaContext.createClient(connect.getServerName(), connect.getHost(), connect.getPort(), connect.getSuffix());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("create OPCUA CLIENT ERROR");
        }
        JSONObject jo = new JSONObject();
        jo.put("lineId", lineId);
        jo.put("serverName", serverName);
        linePushSyncManager.execute(jo.toJSONString());
    }
}
