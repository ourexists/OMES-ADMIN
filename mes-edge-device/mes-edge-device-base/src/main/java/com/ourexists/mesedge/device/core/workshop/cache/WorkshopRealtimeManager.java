package com.ourexists.mesedge.device.core.workshop.cache;

import java.util.List;
import java.util.Map;

/**
 * 场景实时缓存管理器
 */
public interface WorkshopRealtimeManager {

    /**
     * 实时数据处理
     * @param targets
     */
    void realtimeHandle(List<WorkshopRealtime> targets);

    /**
     * 构建缓存模型,模型中的id一定要有
     * @param realtime 场景实时态
     */
    void build(WorkshopRealtime realtime);

    /**
     * 构建缓存模型,模型中的id一定要有
     * @param realtimes
     */
    void build(List<WorkshopRealtime> realtimes);

    void remove(String id);

    void removeBatch(List<String> id);

    void clear();

    Map<String, WorkshopRealtime> getAll();

    WorkshopRealtime get(String id);

    void reload();
}
