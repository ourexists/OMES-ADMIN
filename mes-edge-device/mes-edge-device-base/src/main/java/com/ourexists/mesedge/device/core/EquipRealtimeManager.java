package com.ourexists.mesedge.device.core;

import java.util.List;
import java.util.Map;

public interface EquipRealtimeManager {

    /**
     * @param tenantId 租户id
     * @param equipRealtimeMap key 场景code; values:租户下所有的设备
     */
    void reset(String tenantId, Map<String, List<EquipRealtime>> equipRealtimeMap);

    /**
     * @param tenantId 租户id
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(String tenantId, EquipRealtime equipRealtime);

    void remove(String tenantId, String sn);

    void removeById(String tenantId, String equipId);

    void removeByIds(String tenantId, List<String> equipIds);

    void clear(String tenantId);

    Map<String, List<EquipRealtime>> getAll(String tenantId);

    List<EquipRealtime> getAll(String tenantId, String workshopCode);

    EquipRealtime get(String tenantId, String sn);
}
