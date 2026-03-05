package com.ourexists.omes.device.core.equip.collect;

/**
 * 数据采集器
 */
public interface DataCollector {

    /**
     * 执行采集
     * @param sourceData 来源数据
     */
    void doCollect(String gwId, Object sourceData);

}
