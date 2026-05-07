package com.ourexists.omes.portal.device.cache;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.device.core.equip.cache.*;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipPageQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DEquipRealtimeManager implements EquipRealtimeManager {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EquipFeign equipFeign;

    @Autowired
    private RedisCacheValueConverter cacheValueConverter;

    @Autowired
    private RedissonClient redissonClient;

    private static final String EQUIP_DATA_SYNC_LOCK_KEY = "omes:equip_rt:data_sync";

    @Value("${omes.device.equip-data-sync-lock-wait-seconds:30}")
    private long equipDataSyncLockWaitSeconds;

    private static String equipRedisCacheName(String tenantId) {
        return "{" + tenantId + "}equip_realtime_" + tenantId;
    }

    private static final int RELOAD_PAGE_SIZE = 500;

    private static String idToSnIndexKey(String tenantId) {
        return "{" + tenantId + "}omes:equip_rt:id2sn";
    }

    private Cache tenantCache() {
        String tenantId = UserContext.getTenant().getTenantId();
        Cache springCache = cacheManager.getCache(equipRedisCacheName(tenantId));
        if (springCache == null) {
            throw new BusinessException("缓存未初始化: " + equipRedisCacheName(tenantId));
        }
        return springCache;
    }

    private static String equipCacheRedisKey(String tenantId, String selfCode) {
        return equipRedisCacheName(tenantId) + "::" + selfCode;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public void addOrUpdate(EquipRealtime equipRealtime) {
        withEquipDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            String sn = equipRealtime.getSelfCode();
            luaUpsertEquip(tenantId, sn, equipRealtime);
        });
    }

    @Override
    public void remove(String sn) {
        withEquipDataSyncMutation(() -> {
            Cache.ValueWrapper w = tenantCache().get(sn);
            EquipRealtime er = w == null ? null : cacheValueConverter.convert(w.get(), EquipRealtime.class);
            String tenantId = UserContext.getTenant().getTenantId();
            if (er == null) {
                tenantCache().evict(sn);
                return;
            }
            String idHash = idToSnIndexKey(tenantId);
            String mainKey = equipCacheRedisKey(tenantId, sn);
            EquipRealtimeRedisLua.executeRemove(
                    redisTemplate,
                    Arrays.asList(mainKey, idHash),
                    er.getId(),
                    sn);
        });
    }

    @Override
    public EquipRealtime get(String sn) {
        Cache.ValueWrapper valueWrapper = tenantCache().get(sn);
        return valueWrapper == null ? null : cacheValueConverter.convert(valueWrapper.get(), EquipRealtime.class);
    }

    @Override
    public EquipRealtime getById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        String tenantId = UserContext.getTenant().getTenantId();
        Object sn = stringRedisTemplate.opsForHash().get(idToSnIndexKey(tenantId), id);
        if (!(sn instanceof String selfCode) || !StringUtils.hasText(selfCode)) {
            return null;
        }
        return get(selfCode);
    }

    @Override
    public void reload() {
        RLock lock = redissonClient.getLock(EQUIP_DATA_SYNC_LOCK_KEY);
        boolean locked;
        try {
            locked = lock.tryLock(0, -1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("reload equip realtime interrupted while acquiring Redisson lock");
            return;
        }
        if (!locked) {
            log.info("reload equip realtime skipped: data sync lock busy ({})", EQUIP_DATA_SYNC_LOCK_KEY);
            return;
        }
        try {
            UserContext.defaultTenant();
            try {
                Set<String> tenantsIdIndexReset = new HashSet<>();
                int pageNum = 1;
                while (true) {
                    EquipPageQuery query = new EquipPageQuery();
                    query.setRequirePage(true);
                    query.setPage(pageNum);
                    query.setPageSize(RELOAD_PAGE_SIZE);
                    query.setQueryConfig(true);
                    JsonResponseEntity<List<EquipDto>> response = equipFeign.selectByPage(query);
                    List<EquipDto> equipDtos = response == null ? null : response.getData();
                    if (CollectionUtils.isEmpty(equipDtos)) {
                        break;
                    }
                    putReloadBatch(equipDtos, tenantsIdIndexReset);
                    if (equipDtos.size() < RELOAD_PAGE_SIZE) {
                        break;
                    }
                    pageNum++;
                }
            } catch (Exception e) {
                log.error("reload equip realtime cache failed: {}", e.getMessage(), e);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void putReloadBatch(List<EquipDto> equipDtos, Set<String> tenantsIdIndexReset) {
        Map<String, Map<String, EquipRealtime>> equipRealtimeMap = new HashMap<>();
        for (EquipDto equipDto : equipDtos) {
            String tid = equipDto.getTenantId();
            if (tid != null && tenantsIdIndexReset.add(tid)) {
                try {
                    EquipRealtimeRedisLua.executeClearTenant(
                            redisTemplate,
                            equipRedisCacheName(tid) + "::*",
                            idToSnIndexKey(tid));
                } catch (Exception ex) {
                    log.error("reload: atomic clear tenant redis failed tid={}; stale equip/index may remain", tid, ex);
                }
            }
            if (equipDto.getTenantId() == null) {
                log.warn("reload: skip equip with null tenantId selfCode={}", equipDto.getSelfCode());
                continue;
            }
            Map<String, EquipRealtime> r = equipRealtimeMap.computeIfAbsent(equipDto.getTenantId(), k -> new HashMap<>());
            EquipRealtime equipRealtime = new EquipRealtime();
            BeanUtils.copyProperties(equipDto, equipRealtime);

            EquipRealtimeConfig equipRealtimeConfig = EquipRealtimeConfigFromDto.build(equipDto);
            if (equipRealtimeConfig != null) {
                equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
                equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
                equipRealtime.setEquipControlRealtimes(equipRealtimeConfig.getControls());
                Date currentDate = new Date();
                equipRealtime.setAlarmChangeTime(currentDate);
                equipRealtime.setRunChangeTime(currentDate);
                equipRealtime.setOnlineChangeTime(currentDate);
            }
            r.put(equipDto.getSelfCode(), equipRealtime);
        }
        for (Map.Entry<String, Map<String, EquipRealtime>> entry : equipRealtimeMap.entrySet()) {
            String tenantKey = entry.getKey();
            for (Map.Entry<String, EquipRealtime> snEntry : entry.getValue().entrySet()) {
                String sn = snEntry.getKey();
                EquipRealtime rt = snEntry.getValue();
                luaUpsertEquip(tenantKey, sn, rt);
            }
        }
    }

    private void luaUpsertEquip(String tenantId, String sn, EquipRealtime rt) {
        String idHash = idToSnIndexKey(tenantId);
        String kMain = equipCacheRedisKey(tenantId, sn);
        EquipRealtimeRedisLua.executeUpsert(
                redisTemplate,
                Arrays.asList(kMain, idHash),
                rt,
                EquipRealtimeRedisLua.CACHE_TTL_SECONDS,
                rt.getId(),
                sn);
    }

    private void withEquipDataSyncMutation(Runnable action) {
        RLock lock = redissonClient.getLock(EQUIP_DATA_SYNC_LOCK_KEY);
        try {
            if (!lock.tryLock(equipDataSyncLockWaitSeconds, -1, TimeUnit.SECONDS)) {
                throw new BusinessException("设备实时缓存繁忙，请稍后重试");
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
