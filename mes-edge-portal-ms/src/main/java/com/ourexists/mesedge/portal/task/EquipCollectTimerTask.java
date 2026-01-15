package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipCollectFeign;
import com.ourexists.mesedge.device.model.EquipCollectDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component("EquipCollect")
public class EquipCollectTimerTask extends TimerTask {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private EquipCollectFeign equipCollectFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        Map<String, EquipRealtime> equipRealtimeMap = equipRealtimeManager.getAll(CommonConstant.SYSTEM_TENANT);
        Date now = new Date();
        List<EquipCollectDto> dtos = new ArrayList<>();
        for (EquipRealtime equipRealtime : equipRealtimeMap.values()) {
            List<EquipAttrRealtime> equipAttrRealtimes = equipRealtime.getEquipAttrRealtimes();
            if (CollectionUtils.isEmpty(equipAttrRealtimes)) {
                continue;
            }

            Map<String, String> data = new HashMap<>();
            for (EquipAttrRealtime equipAttrRealtime : equipAttrRealtimes) {
                if (equipAttrRealtime.getNeedCollect() == null || !equipAttrRealtime.getNeedCollect()) {
                    continue;
                }
                data.put(equipAttrRealtime.getName(), equipAttrRealtime.getValue());
            }
            if (CollectionUtils.isEmpty(data)) {
                continue;
            }
            EquipCollectDto equipCollectDto = new EquipCollectDto();
            equipCollectDto.setSn(equipRealtime.getSelfCode());
            equipCollectDto.setTime(now);
            equipCollectDto.setData(data);
            equipCollectDto.setTenantId(equipRealtime.getTenantId());
            dtos.add(equipCollectDto);
        }
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }
        try {
            RemoteHandleUtils.getDataFormResponse(equipCollectFeign.addBatch(dtos));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
