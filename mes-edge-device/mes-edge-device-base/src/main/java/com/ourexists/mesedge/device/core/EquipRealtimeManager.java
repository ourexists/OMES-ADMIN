package com.ourexists.mesedge.device.core;

import java.util.List;
import java.util.Map;

public interface EquipRealtimeManager {

    /**
     * 实时数据处理
     * @param tenantId
     * @param targets
     */
    void realtimeHandle(String tenantId, List<EquipRealtime> targets);

    /**
     * @param tenantId 租户id
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(String tenantId, EquipRealtime equipRealtime);

    void remove(String tenantId, String sn);

    void removeBatch(String tenantId, List<String> ids);

    void clear(String tenantId);

    Map<String, EquipRealtime> getAll();

    Map<String, EquipRealtime> getAll(String tenantId);

    EquipRealtime get(String tenantId, String sn);

    EquipRealtime getById(String tenantId, String id);

    void reload();
}
