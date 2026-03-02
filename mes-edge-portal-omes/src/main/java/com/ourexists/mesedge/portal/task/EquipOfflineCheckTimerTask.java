package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtime;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.mesedge.task.process.task.TimerTask;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("EquipOfflineCheck")
public class EquipOfflineCheckTimerTask extends TimerTask {

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
                Map<String, EquipRealtime> equipRealtimeMap = equipRealtimeManager.getAll();

                List<EquipRealtime> targets = new ArrayList<>();
                for (EquipRealtime realtime : equipRealtimeMap.values()) {
                    if (realtime.getTime() == null) {
                        continue;
                    }
                    if (realtime.getOnlineState() == 0) {
                        continue;
                    }
                    long diffMillis = Math.abs(now.getTime() - realtime.getTime().getTime());
                    long diffMinutes = diffMillis / (60 * 1000); // 转换为分钟
                    if (diffMinutes > 5) {
                        EquipRealtime target = new EquipRealtime();
                        BeanUtils.copyProperties(realtime, target);
                        target.offline();
                        targets.add(target);
                    }
                }
                if (CollectionUtils.isEmpty(targets)) {
                    return;
                }
                equipRealtimeManager.realtimeHandle(targets);
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
