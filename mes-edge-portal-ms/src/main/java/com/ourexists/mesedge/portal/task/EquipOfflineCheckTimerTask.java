package com.ourexists.mesedge.portal.task;

import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Component("EquipOfflineCheck")
public class EquipOfflineCheckTimerTask extends TimerTask {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public void doRun() {
        UserContext.defaultTenant();
        Map<String, EquipRealtime> equipRealtimeMap = equipRealtimeManager.getAll(CommonConstant.SYSTEM_TENANT);
        Date now = new Date();
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
                realtime.offline();
            }
        }
        equipRealtimeManager.reset(CommonConstant.SYSTEM_TENANT, equipRealtimeMap);
    }
}
