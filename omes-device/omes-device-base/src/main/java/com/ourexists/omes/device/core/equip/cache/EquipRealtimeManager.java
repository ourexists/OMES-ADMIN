package com.ourexists.omes.device.core.equip.cache;

import java.util.List;

public interface EquipRealtimeManager {

    /**
     * @param equipRealtime 设备实时态
     */
    void addOrUpdate(EquipRealtime equipRealtime);

    void remove(String sn);

    void clear();

    EquipRealtime get(String sn);

    EquipRealtime getById(String id);

    /** 按采集网关 ID 返回缓存中的设备实时对象列表；顺序无保证，索引与主缓存不一致时可能少于 Set 成员数 */
    List<EquipRealtime> listByGwId(String gwId);

    void reload();
}
