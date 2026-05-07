package com.ourexists.omes.device.core.equip.cache;

import java.util.List;

/**
 * 设备 {@link EquipRealtimeConfig} 独立缓存（与 {@link EquipRealtimeManager} 无依赖；各自 reload / 写入）。
 * gw→sn、equipId→selfCode 索引由本管理器维护（Lua 原子更新）；按网关查序列号 {@link #listSelfCodeByGwId}，按设备 id 查配置 {@link #getByEquipId}。
 */
public interface EquipRealtimeConfigManager {

    /**
     * @param tenantId 租户 ID
     * @param selfCode 设备序列号（与主缓存 key 一致）
     * @param config   为 null 时删除该设备配置缓存
     */
    void addOrUpdate(String tenantId, String selfCode, EquipRealtimeConfig config);

    void remove(String tenantId, String selfCode);

    EquipRealtimeConfig get(String tenantId, String selfCode);

    /** 通过设备主键解析配置（依赖 id→selfCode 索引） */
    EquipRealtimeConfig getByEquipId(String tenantId, String equipId);

    /** 与 {@link EquipRealtimeManager#reload()} 独立：仅刷新本管理器负责的 Redis 配置缓存 */
    void reload();

    /** 按采集网关 ID 返回配置缓存中已索引的设备序列号；顺序无保证 */
    List<String> listSelfCodeByGwId(String tenantId, String gwId);
}
