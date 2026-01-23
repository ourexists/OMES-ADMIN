/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipStateSnapshotFeign;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("EquipStateSnapshot")
public class EquipStateSnapshotTimerTask extends TimerTask {

    @Autowired
    private EquipStateSnapshotFeign equipStateSnapshotFeign;

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public void doRun() {
        Date date = new Date();
        List<EquipStateSnapshotDto> dtos = new ArrayList<>();
        Map<String, EquipRealtime> map = equipRealtimeManager.getAll();
        for (EquipRealtime value : map.values()) {
            EquipStateSnapshotDto equipStateSnapshotDto = new EquipStateSnapshotDto()
                    .setSn(value.getSelfCode())
                    .setRunState(value.getRunState())
                    .setAlarmState(value.getAlarmState())
                    .setOnlineState(value.getOnlineState())
                    .setTime(date);
            dtos.add(equipStateSnapshotDto);
        }
        try {
            RemoteHandleUtils.getDataFormResponse(equipStateSnapshotFeign.addBatch(dtos));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
