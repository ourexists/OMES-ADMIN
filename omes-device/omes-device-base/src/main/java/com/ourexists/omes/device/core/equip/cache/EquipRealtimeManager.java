package com.ourexists.omes.device.core.equip.cache;

import java.util.Map;

public interface EquipRealtimeManager {

    /**
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(EquipRealtime equipRealtime);

    void remove(String sn);

    void clear();

    Map<String, EquipRealtime> getAll();

    EquipRealtime get(String sn);

    EquipRealtime getById(String id);

    void reload();
}
