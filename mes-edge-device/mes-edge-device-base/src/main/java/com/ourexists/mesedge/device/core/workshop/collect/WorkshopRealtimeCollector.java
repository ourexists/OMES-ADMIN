package com.ourexists.mesedge.device.core.workshop.collect;

import java.util.List;

/**
 * 设备实时采集器
 */
public interface WorkshopRealtimeCollector {

    /**
     * 采集器名称
     * @return
     */
    String name();

    /**
     * 执行采集
     * @param source 来源数据
     */
    void doCollect(Object source);


    /**
     * 所有需要采集的变量
     * @return
     */
    List<String> collectVariables();
}
