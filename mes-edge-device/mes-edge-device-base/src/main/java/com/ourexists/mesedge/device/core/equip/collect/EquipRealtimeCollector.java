package com.ourexists.mesedge.device.core.equip.collect;

/**
 * 设备实时采集器
 */
public interface EquipRealtimeCollector {

    /**
     * 采集器名称
     * @return
     */
    String name();

    /**
     * 执行采集
     * @param sourceData 来源数据
     */
    void doCollect(Object sourceData);
}
