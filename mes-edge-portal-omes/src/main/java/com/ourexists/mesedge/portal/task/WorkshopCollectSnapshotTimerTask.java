package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.mesedge.device.feign.WorkshopCollectFeign;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import com.ourexists.mesedge.task.process.task.TimerTask;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component("WorkshopCollectSnapshot")
public class WorkshopCollectSnapshotTimerTask extends TimerTask {

    @Autowired
    private WorkshopRealtimeManager workshopRealtimeManager;

    @Autowired
    private TenantFeign tenantFeign;

    @Autowired
    private WorkshopCollectFeign workshopCollectFeign;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        UserContext.getTenant().setSkipMain(false);
        Date now = new Date();
        try {
            List<TenantVo> tenantVos = RemoteHandleUtils.getDataFormResponse(tenantFeign.all());
            for (TenantVo tenantVo : tenantVos) {
                UserContext.getTenant().setTenantId(tenantVo.getTenantCode());
                List<WorkshopCollectDto> dtos = new ArrayList<>();
                Map<String, WorkshopRealtime> workshopRealtimeMap = workshopRealtimeManager.getAll();
                for (WorkshopRealtime workshopRealtime : workshopRealtimeMap.values()) {
                    List<WorkshopRealtimeCollect> workshopRealtimeCollects = workshopRealtime.getAttrsRealtime();
                    if (CollectionUtils.isEmpty(workshopRealtimeCollects)) {
                        continue;
                    }

                    Map<String, String> data = new HashMap<>();
                    for (WorkshopRealtimeCollect workshopRealtimeCollect : workshopRealtimeCollects) {
                        if (workshopRealtimeCollect.getNeedCollect() == null || !workshopRealtimeCollect.getNeedCollect()) {
                            continue;
                        }
                        data.put(workshopRealtimeCollect.getName(), workshopRealtimeCollect.getValue());
                    }
                    if (CollectionUtils.isEmpty(data)) {
                        continue;
                    }
                    WorkshopCollectDto WorkshopCollectDto = new WorkshopCollectDto();
                    WorkshopCollectDto.setWorkshopId(workshopRealtime.getId());
                    WorkshopCollectDto.setTime(now);
                    WorkshopCollectDto.setData(data);
                    dtos.add(WorkshopCollectDto);
                }
                RemoteHandleUtils.getDataFormResponse(workshopCollectFeign.addBatch(dtos));
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
