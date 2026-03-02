package com.ourexists.omes.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.EquipAttrRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.feign.EquipCollectFeign;
import com.ourexists.omes.device.model.EquipCollectDto;
import com.ourexists.omes.task.process.task.TimerTask;
import com.ourexists.omes.ucenter.feign.TenantFeign;
import com.ourexists.omes.ucenter.tenant.TenantVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component("EquipCollectSnapshot")
public class EquipCollectSnapshotTimerTask extends TimerTask {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private EquipCollectFeign equipCollectFeign;

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
                Map<String, EquipRealtime> equipRealtimeMap = equipRealtimeManager.getAll();
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
                RemoteHandleUtils.getDataFormResponse(equipCollectFeign.addBatch(dtos));
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
