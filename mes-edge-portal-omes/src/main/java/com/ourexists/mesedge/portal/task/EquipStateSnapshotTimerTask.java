/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtime;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipStateSnapshotFeign;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
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

    @Autowired
    private TenantFeign tenantFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        UserContext.getTenant().setSkipMain(false);
        Date now = new Date();
        try {
            List<TenantVo> tenantVos = RemoteHandleUtils.getDataFormResponse(tenantFeign.all());
            for (TenantVo tenantVo : tenantVos) {
                UserContext.getTenant().setTenantId(tenantVo.getTenantCode());
                List<EquipStateSnapshotDto> dtos = new ArrayList<>();
                Map<String, EquipRealtime> map = equipRealtimeManager.getAll();
                for (EquipRealtime value : map.values()) {
                    EquipStateSnapshotDto equipStateSnapshotDto = new EquipStateSnapshotDto().setSn(value.getSelfCode()).setRunState(value.getRunState()).setAlarmState(value.getAlarmState()).setOnlineState(value.getOnlineState()).setTime(now);
                    dtos.add(equipStateSnapshotDto);
                }
                RemoteHandleUtils.getDataFormResponse(equipStateSnapshotFeign.addBatch(dtos));
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
