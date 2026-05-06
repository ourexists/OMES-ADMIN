package com.ourexists.omes.portal.device.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;

/**
 * 车间场景实时：主缓存与 gw→场景 Set 一次 Lua 原子写入/删除；清空走 SCAN。
 * <p>Redis Cluster 要求脚本内 KEYS 同 slot；调用方须使用同一 {@code {tenantId}} hash tag。
 */
final class WorkshopRealtimeRedisLua {

    private static final DefaultRedisScript<Long> UPSERT_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> REMOVE_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> CLEAR_TENANT_SCRIPT = new DefaultRedisScript<>();

    static {
        UPSERT_SCRIPT.setScriptText("""
                redis.call('SET', KEYS[1], ARGV[1], 'EX', tonumber(ARGV[2]))
                local wid = ARGV[3]
                local flags = ARGV[4]
                if flags ~= nil and flags ~= '' then
                  for i = 2, #KEYS do
                    local f = string.sub(flags, i - 1, i - 1)
                    if f == 'o' then
                      redis.call('SREM', KEYS[i], wid)
                    elseif f == 'n' or f == 'b' then
                      redis.call('SADD', KEYS[i], wid)
                    end
                  end
                end
                return 1
                """);
        UPSERT_SCRIPT.setResultType(Long.class);

        REMOVE_SCRIPT.setScriptText("""
                redis.call('DEL', KEYS[1])
                local wid = ARGV[1]
                for i = 2, #KEYS do
                  redis.call('SREM', KEYS[i], wid)
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
                return 1
                """);
        CLEAR_TENANT_SCRIPT.setResultType(Long.class);
    }

    /** 与 CacheConfig 中 RedisCache 默认 TTL 一致（分钟） */
    static final int CACHE_TTL_SECONDS = 30 * 60;

    private WorkshopRealtimeRedisLua() {
    }

    /**
     * KEYS[1] 主缓存；KEYS[2..] 为涉及的 gw Set，顺序与 ARGV[4] 逐字符对应（o=仅删、n=仅加、b=仍绑定仅 SADD 幂等）。
     */
    static void executeUpsert(
            RedisTemplate<String, Object> redisTemplate,
            List<String> keys,
            Object workshopPayload,
            int ttlSeconds,
            String workshopId,
            String gwFlags) {
        redisTemplate.execute(
                UPSERT_SCRIPT,
                keys,
                workshopPayload,
                ttlSeconds,
                workshopId,
                gwFlags == null ? "" : gwFlags);
    }

    /** KEYS[1] 主缓存；KEYS[2..] 为需 SREM 的 gw Set（可无） */
    static void executeRemove(
            RedisTemplate<String, Object> redisTemplate,
            List<String> keys,
            String workshopId) {
        redisTemplate.execute(
                REMOVE_SCRIPT,
                keys,
                workshopId);
    }

    static void executeClearTenant(
            RedisTemplate<String, Object> redisTemplate,
            String routingKey,
            String workshopKeyPattern,
            String gwToWorkshopKeyPattern) {
        redisTemplate.execute(
                CLEAR_TENANT_SCRIPT,
                Collections.singletonList(routingKey),
                workshopKeyPattern,
                gwToWorkshopKeyPattern);
    }
}
