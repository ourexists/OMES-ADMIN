package com.ourexists.mesedge.device.core;

import java.util.Map;

public interface EquipRealtimeManager {

    /**
     * @param tenantId 租户id
     * @param equipRealtimeMap key 设备编号; values:租户下所有的设备
     */
    void reset(String tenantId, Map<String, EquipRealtime> equipRealtimeMap);

    /**
     * @param tenantId 租户id
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(String tenantId, EquipRealtime equipRealtime);

    void remove(String tenantId, String sn);

    void clear(String tenantId);

    Map<String, EquipRealtime> getAll(String tenantId);

    EquipRealtime get(String tenantId, String sn);
}
