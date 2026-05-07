package com.ourexists.omes.device.core.equip.cache;

public interface EquipRealtimeManager {

    /**
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(EquipRealtime equipRealtime);

    void remove(String sn);

    EquipRealtime get(String sn);

    EquipRealtime getById(String id);

    void reload();
}
