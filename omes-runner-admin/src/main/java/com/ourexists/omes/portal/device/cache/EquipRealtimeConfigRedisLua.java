package com.ourexists.omes.portal.device.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;

/**
 * 设备配置 Spring RedisCache 主键、gw→sn Set、equipId→selfCode Hash 的一次性原子写入（Redis Lua）。
 */
final class EquipRealtimeConfigRedisLua {

    private static final DefaultRedisScript<Long> UPSERT_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> REMOVE_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> CLEAR_TENANT_SCRIPT = new DefaultRedisScript<>();

    static {
        UPSERT_SCRIPT.setScriptText("""
                redis.call('SET', KEYS[1], ARGV[1], 'EX', tonumber(ARGV[2]))
                if ARGV[3] == '1' then
                  redis.call('SREM', KEYS[2], ARGV[4])
                end
                if ARGV[5] == '1' then
                  redis.call('SADD', KEYS[3], ARGV[4])
                end
                if ARGV[6] ~= '' then
                  redis.call('HSET', KEYS[4], ARGV[6], ARGV[4])
                end
                if ARGV[7] ~= '' then
                  redis.call('HDEL', KEYS[4], ARGV[7])
                end
                return 1
                """);
        UPSERT_SCRIPT.setResultType(Long.class);

        REMOVE_SCRIPT.setScriptText("""
                redis.call('DEL', KEYS[1])
                if ARGV[1] == '1' then
                  redis.call('SREM', KEYS[2], ARGV[2])
                end
                if ARGV[3] ~= '' then
                  redis.call('HDEL', KEYS[3], ARGV[3])
                end
                return 1
                """);
        REMOVE_SCRIPT.setResultType(Long.class);

        CLEAR_TENANT_SCRIPT.setScriptText("""
                local function del_by_pattern(pattern)
                  local cursor = '0'
                  repeat
                    local reply = redis.call('SCAN', cursor, 'MATCH', pattern, 'COUNT', 500)
                    cursor = reply[1]
                    local ks = reply[2]
                    for i = 1, #ks do
                      redis.call('DEL', ks[i])
                    end
                  until cursor == '0'
                end
                del_by_pattern(ARGV[1])
                del_by_pattern(ARGV[2])
                redis.call('DEL', KEYS[1])
                return 1
                """);
        CLEAR_TENANT_SCRIPT.setResultType(Long.class);
    }

    static final int CACHE_TTL_SECONDS = EquipRealtimeRedisLua.CACHE_TTL_SECONDS;

    private EquipRealtimeConfigRedisLua() {
    }

    static void executeUpsert(
            RedisTemplate<String, Object> redisTemplate,
            List<String> keys,
            Object configSerializedPayload,
            int ttlSeconds,
            String selfCode,
            boolean sremOldGw,
            boolean saddNewGw,
            String newEquipId,
            String oldEquipIdToHdel) {
        redisTemplate.execute(
                UPSERT_SCRIPT,
                keys,
                configSerializedPayload,
                ttlSeconds,
                sremOldGw ? "1" : "0",
                selfCode,
                saddNewGw ? "1" : "0",
                newEquipId == null ? "" : newEquipId,
                oldEquipIdToHdel == null ? "" : oldEquipIdToHdel);
    }

    static void executeRemove(
            RedisTemplate<String, Object> redisTemplate,
            List<String> keys,
            String selfCode,
            boolean sremGw,
            String equipIdForHdel) {
        redisTemplate.execute(
                REMOVE_SCRIPT,
                keys,
                sremGw ? "1" : "0",
                selfCode,
                equipIdForHdel == null ? "" : equipIdForHdel);
    }

    /**
     * @param idToSnHashKey 与配置缓存同 hash tag 的 equipId→selfCode Hash；脚本结束时 DEL 以清空索引
     */
    static void executeClearTenantConfigKeys(
            RedisTemplate<String, Object> redisTemplate,
            String idToSnHashKey,
            String equipConfigKeyPattern,
            String gwToSnKeyPattern) {
        redisTemplate.execute(
                CLEAR_TENANT_SCRIPT,
                Collections.singletonList(idToSnHashKey),
                equipConfigKeyPattern,
                gwToSnKeyPattern);
    }
}
