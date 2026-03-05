package com.ourexists.omes.device.core.equip.collect;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据采集器链
 */
@Component
public class DataCollectChain {

    private List<DataCollector> collectors;

    public DataCollectChain(
            List<DataCollector> collectors) {
        this.collectors = collectors;
    }

    public void doCollect(String gwId, String payload) {
        for (DataCollector r : collectors) {
            r.doCollect(gwId, payload);
        }
    }
}
