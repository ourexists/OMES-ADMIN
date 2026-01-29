package com.ourexists.mesedge.device.core.equip.cache;

import java.util.List;
import java.util.Map;

public interface EquipRealtimeManager {

    /**
     * 实时数据处理
     * @param targets
     */
    void realtimeHandle(List<EquipRealtime> targets);

    /**
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(EquipRealtime equipRealtime);

    void remove(String sn);

    void removeBatch(List<String> ids);

    void clear();

    Map<String, EquipRealtime> getAll();

    EquipRealtime get(String sn);

    EquipRealtime getById(String id);

    void reload();
}
