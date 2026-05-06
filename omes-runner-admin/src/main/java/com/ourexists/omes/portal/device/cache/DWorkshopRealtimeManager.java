package com.ourexists.omes.portal.device.cache;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeConfig;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.WorkshopConfigCollectAttr;
import com.ourexists.omes.device.model.WorkshopConfigCollectDetail;
import com.ourexists.omes.device.model.WorkshopConfigCollectDto;
import com.ourexists.omes.ucenter.feign.TenantFeign;
import com.ourexists.omes.ucenter.tenant.TenantVo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DWorkshopRealtimeManager implements WorkshopRealtimeManager {

    private final CacheManager cacheManager;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WorkshopFeign workshopFeign;
    private final TenantFeign tenantFeign;
    private final RedisCacheValueConverter cacheValueConverter;
    private final RedissonClient redissonClient;

    /**
     * 串行化 {@link #reload} 与写路径（{@link #build}、{@link #realtimeHandle}、{@link #remove}、{@link #clear}），
     * 对齐 {@link DEquipRealtimeManager}。
     */
    private static final String WORKSHOP_DATA_SYNC_LOCK_KEY = "omes:workshop_rt:data_sync";

    @Value("${omes.device.workshop-data-sync-lock-wait-seconds:30}")
    private long workshopDataSyncLockWaitSeconds;

    public DWorkshopRealtimeManager(
            CacheManager cacheManager,
            StringRedisTemplate stringRedisTemplate,
            RedisTemplate<String, Object> redisTemplate,
            WorkshopFeign workshopFeign,
            TenantFeign tenantFeign,
            RedisCacheValueConverter cacheValueConverter,
            RedissonClient redissonClient) {
        this.cacheManager = cacheManager;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.workshopFeign = workshopFeign;
        this.tenantFeign = tenantFeign;
        this.cacheValueConverter = cacheValueConverter;
        this.redissonClient = redissonClient;
    }

    /**
     * Spring RedisCache 名；{@code {tenantId}} 保证 Cluster 与 Lua 清空同 slot。
     */
    private static String workshopRedisCacheName(String tenantId) {
        return "{" + tenantId + "}WORKSHOP_REALTIME_" + tenantId;
    }

    /** Cluster 脚本路由 key（可不存值，仅 slot 计算） */
    private static String workshopClusterRoutingKey(String tenantId) {
        return "{" + tenantId + "}omes:workshop_rt:cluster";
    }

    /** Redis Set：member=场景 id（workshopId），与主缓存同 slot */
    private static String gwToWorkshopSetKey(String tenantId, String gwId) {
        return "{" + tenantId + "}omes:workshop_rt:gw2ws:" + gwId;
    }

    private static String gwToWorkshopKeyPattern(String tenantId) {
        return "{" + tenantId + "}omes:workshop_rt:gw2ws:*";
    }

    /** 与 Spring RedisCache 的 Redis 主键一致 */
    private static String workshopMainRedisKey(String tenantId, String workshopId) {
        return workshopRedisCacheName(tenantId) + "::" + workshopId;
    }

    @PostConstruct
    public void init() {
        UserContext.defaultTenant();
        UserContext.getTenant().setSkipMain(false);
        try {
            List<TenantVo> tenantVos = RemoteHandleUtils.getDataFormResponse(tenantFeign.all());
            for (TenantVo tenantVo : tenantVos) {
                UserContext.getTenant().setTenantId(tenantVo.getTenantCode());
                reload();
            }
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
    }

    private Cache tenantCache() {
        String tenantId = UserContext.getTenant().getTenantId();
        Cache springCache = cacheManager.getCache(workshopRedisCacheName(tenantId));
        if (springCache == null) {
            throw new BusinessException("缓存未初始化: " + workshopRedisCacheName(tenantId));
        }
        return springCache;
    }

    private Map<String, WorkshopRealtime> getAllByTenant(String tenantId) {
        Cache cache = cacheManager.getCache(workshopRedisCacheName(tenantId));
        if (cache == null) {
            return new HashMap<>();
        }
        String name = workshopRedisCacheName(tenantId);
        String prefix = name + "::";
        String pattern = name + "::*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();
        Map<String, WorkshopRealtime> result = new HashMap<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String redisKey = cursor.next();
                if (!redisKey.startsWith(prefix)) {
                    continue;
                }
                String cacheKey = redisKey.substring(prefix.length());
                Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
                WorkshopRealtime workshopRealtime =
                        valueWrapper == null ? null : cacheValueConverter.convert(valueWrapper.get(), WorkshopRealtime.class);
                if (workshopRealtime != null) {
                    result.put(cacheKey, workshopRealtime);
                }
            }
        }
        return result;
    }

    @Override
    public void realtimeHandle(List<WorkshopRealtime> targets) {
        withWorkshopDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            for (WorkshopRealtime target : targets) {
                if (!StringUtils.hasText(target.getId())) {
                    throw new IllegalArgumentException("[WorkshopRealtime] id is required");
                }
                WorkshopRealtime source = get(target.getId());
                if (source == null) {
                    continue;
                }
                Set<String> oldGws = collectGwIds(source);
                source.setAttrsRealtime(target.getAttrsRealtime());
                source.setTime(new Date());
                Set<String> newGws = collectGwIds(source);
                luaUpsertWorkshop(tenantId, source, oldGws, newGws);
            }
        });
    }

    @Override
    public void build(WorkshopRealtime realtime) {
        withWorkshopDataSyncMutation(() -> buildUnlocked(Collections.singletonList(realtime)));
    }

    @Override
    public void build(List<WorkshopRealtime> realtimes) {
        withWorkshopDataSyncMutation(() -> buildUnlocked(realtimes));
    }

    private void buildUnlocked(List<WorkshopRealtime> realtimes) {
        String tenantId = UserContext.getTenant().getTenantId();
        Cache cache = tenantCache();
        for (WorkshopRealtime realtime : realtimes) {
            if (!StringUtils.hasText(realtime.getId())) {
                throw new IllegalArgumentException("[WorkshopRealtime] id is required");
            }
            if (realtime.getAttrsRealtime() == null) {
                if (realtime.getConfig() != null && realtime.getConfig().getAttrs() != null) {
                    realtime.setAttrsRealtime(realtime.getConfig().getAttrs());
                }
            }
            Cache.ValueWrapper oldW = cache.get(realtime.getId());
            WorkshopRealtime old =
                    oldW == null ? null : cacheValueConverter.convert(oldW.get(), WorkshopRealtime.class);
            Set<String> oldGws = collectGwIds(old);
            luaUpsertWorkshop(tenantId, realtime, oldGws, collectGwIds(realtime));
        }
    }

    @Override
    public void remove(String id) {
        withWorkshopDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            WorkshopRealtime er = get(id);
            if (er == null) {
                tenantCache().evict(id);
                return;
            }
            luaRemoveWorkshop(tenantId, id, er);
        });
    }

    @Override
    public void clear() {
        withWorkshopDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            try {
                WorkshopRealtimeRedisLua.executeClearTenant(
                        redisTemplate,
                        workshopClusterRoutingKey(tenantId),
                        workshopRedisCacheName(tenantId) + "::*",
                        gwToWorkshopKeyPattern(tenantId));
            } catch (Exception ex) {
                log.error("clear tenant workshop realtime (lua) failed: tenantId={}", tenantId, ex);
                throw new BusinessException("清空车间实时缓存失败");
            }
        });
    }

    @Override
    public Map<String, WorkshopRealtime> getAll() {
        return getAllByTenant(UserContext.getTenant().getTenantId());
    }

    @Override
    public WorkshopRealtime get(String id) {
        Cache.ValueWrapper valueWrapper = tenantCache().get(id);
        return valueWrapper == null ? null : cacheValueConverter.convert(valueWrapper.get(), WorkshopRealtime.class);
    }

    @Override
    public List<WorkshopRealtime> listByGwId(String gwId) {
        if (!StringUtils.hasText(gwId)) {
            return Collections.emptyList();
        }
        String tenantId = UserContext.getTenant().getTenantId();
        Set<String> workshopIds;
        try {
            workshopIds = stringRedisTemplate.opsForSet().members(gwToWorkshopSetKey(tenantId, gwId));
        } catch (Exception ex) {
            log.error("listByGwId SMEMBERS failed: tenantId={} gwId={}", tenantId, gwId, ex);
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(workshopIds)) {
            return Collections.emptyList();
        }
        List<WorkshopRealtime> list = new ArrayList<>(workshopIds.size());
        for (String wid : workshopIds) {
            if (!StringUtils.hasText(wid)) {
                continue;
            }
            WorkshopRealtime rt = get(wid);
            if (rt != null) {
                list.add(rt);
            }
        }
        return list;
    }

    @Override
    public void reload() {
        RLock lock = redissonClient.getLock(WORKSHOP_DATA_SYNC_LOCK_KEY);
        boolean locked;
        try {
            locked = lock.tryLock(0, -1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("reload workshop realtime interrupted while acquiring Redisson lock");
            return;
        }
        if (!locked) {
            log.info("reload workshop realtime skipped: data sync lock busy ({})", WORKSHOP_DATA_SYNC_LOCK_KEY);
            return;
        }
        try {
            String tenantId = UserContext.getTenant().getTenantId();
            try {
                WorkshopRealtimeRedisLua.executeClearTenant(
                        redisTemplate,
                        workshopClusterRoutingKey(tenantId),
                        workshopRedisCacheName(tenantId) + "::*",
                        gwToWorkshopKeyPattern(tenantId));
            } catch (Exception ex) {
                log.error("reload: clear workshop tenant redis failed tenantId={}", tenantId, ex);
            }
            List<WorkshopConfigCollectDto> dtos =
                    RemoteHandleUtils.getDataFormResponse(workshopFeign.queryAllConfigCollect());
            List<WorkshopRealtime> realtimes = new ArrayList<>();
            for (WorkshopConfigCollectDto dto : dtos) {
                WorkshopConfigCollectDetail config = dto.getConfig();
                if (config == null) {
                    continue;
                }
                WorkshopRealtime workshopRealtime = new WorkshopRealtime();
                workshopRealtime.setId(dto.getWorkshopId());
                WorkshopRealtimeConfig configRealtime = new WorkshopRealtimeConfig();
                BeanUtils.copyProperties(config, configRealtime);
                List<WorkshopRealtimeCollect> attrs = toRealtimeCollectList(config);
                if (!attrs.isEmpty()) {
                    configRealtime.setAttrs(attrs);
                }
                workshopRealtime.setConfig(configRealtime);
                realtimes.add(workshopRealtime);
            }
            buildUnlocked(realtimes);
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private List<WorkshopRealtimeCollect> toRealtimeCollectList(WorkshopConfigCollectDetail config) {
        List<WorkshopRealtimeCollect> result = new ArrayList<>();
        if (config != null && !CollectionUtils.isEmpty(config.getAttrs())) {
            for (WorkshopConfigCollectAttr attr : config.getAttrs()) {
                result.add(toRealtimeCollect(attr));
            }
        }
        return result;
    }

    private WorkshopRealtimeCollect toRealtimeCollect(WorkshopConfigCollectAttr attr) {
        WorkshopRealtimeCollect c = new WorkshopRealtimeCollect();
        BeanUtils.copyProperties(attr, c);
        return c;
    }

    /** 从采集项上收集非空 gwId（优先 attrsRealtime，否则 config.attrs） */
    private static Set<String> collectGwIds(WorkshopRealtime rt) {
        if (rt == null) {
            return Collections.emptySet();
        }
        List<WorkshopRealtimeCollect> attrs = rt.getAttrsRealtime();
        if (CollectionUtils.isEmpty(attrs) && rt.getConfig() != null) {
            attrs = rt.getConfig().getAttrs();
        }
        if (CollectionUtils.isEmpty(attrs)) {
            return Collections.emptySet();
        }
        Set<String> set = new LinkedHashSet<>();
        for (WorkshopRealtimeCollect a : attrs) {
            if (a != null && StringUtils.hasText(a.getGwId())) {
                set.add(a.getGwId());
            }
        }
        return set;
    }

    /**
     * flags：对排序后的 gw 并集，o=仅 SREM、n=仅 SADD、b=仍绑定仅 SADD。
     */
    private void luaUpsertWorkshop(String tenantId, WorkshopRealtime rt, Set<String> oldGws, Set<String> newGws) {
        Set<String> union = new TreeSet<>();
        union.addAll(oldGws);
        union.addAll(newGws);
        List<String> keys = new ArrayList<>(1 + union.size());
        keys.add(workshopMainRedisKey(tenantId, rt.getId()));
        StringBuilder flags = new StringBuilder();
        for (String gw : union) {
            keys.add(gwToWorkshopSetKey(tenantId, gw));
            boolean o = oldGws.contains(gw);
            boolean n = newGws.contains(gw);
            flags.append(o && !n ? 'o' : (!o && n ? 'n' : 'b'));
        }
        WorkshopRealtimeRedisLua.executeUpsert(
                redisTemplate,
                keys,
                rt,
                WorkshopRealtimeRedisLua.CACHE_TTL_SECONDS,
                rt.getId(),
                flags.toString());
    }

    private void luaRemoveWorkshop(String tenantId, String workshopId, WorkshopRealtime er) {
        Set<String> gws = collectGwIds(er);
        List<String> keys = new ArrayList<>(1 + gws.size());
        keys.add(workshopMainRedisKey(tenantId, workshopId));
        for (String gw : new TreeSet<>(gws)) {
            keys.add(gwToWorkshopSetKey(tenantId, gw));
        }
        WorkshopRealtimeRedisLua.executeRemove(redisTemplate, keys, workshopId);
    }

    private void withWorkshopDataSyncMutation(Runnable action) {
        RLock lock = redissonClient.getLock(WORKSHOP_DATA_SYNC_LOCK_KEY);
        try {
            if (!lock.tryLock(workshopDataSyncLockWaitSeconds, -1, TimeUnit.SECONDS)) {
                throw new BusinessException("车间实时缓存繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        }
        try {
            action.run();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
