package com.ourexists.omes.device.core.workshop.cache;

import java.util.List;

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

    void clear();

    WorkshopRealtime get(String id);

    /** 按采集网关 ID 返回缓存中的场景实时对象列表；顺序无保证，索引与主缓存不一致时可能少于 Set 成员数 */
    List<WorkshopRealtime> listByGwId(String gwId);

    void reload();
}
