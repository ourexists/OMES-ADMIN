package com.ourexists.omes.portal.device.cache;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeConfig;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeConfigManager;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipPageQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 设备配置缓存 + gw→sn Set + equipId→selfCode Hash（Lua 原子写入）；与 {@link DEquipRealtimeManager} 互不注入。
 */
@Slf4j
@Component
public class DEquipRealtimeConfigManager implements EquipRealtimeConfigManager {

    private static final String EQUIP_CONFIG_SYNC_LOCK_KEY = "omes:equip_rt_cfg:data_sync";
    private static final int RELOAD_PAGE_SIZE = 500;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisCacheValueConverter cacheValueConverter;

    @Autowired
    private EquipFeign equipFeign;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    static String equipRtConfigCacheName(String tenantId) {
        return "{" + tenantId + "}equip_rt_config_" + tenantId;
    }

    private static String configCacheRedisKey(String tenantId, String selfCode) {
        return equipRtConfigCacheName(tenantId) + "::" + selfCode;
    }

    private static String idToSnIndexKey(String tenantId) {
        return "{" + tenantId + "}omes:equip_rt_cfg:id2sn";
    }

    private static String gwToSnSetKey(String tenantId, String gwId) {
        return "{" + tenantId + "}omes:equip_rt_cfg:gw2sn:" + gwId;
    }

    private static String gwToSnKeyPattern(String tenantId) {
        return "{" + tenantId + "}omes:equip_rt_cfg:gw2sn:*";
    }

    private static String resolveGwId(EquipRealtimeConfig cfg) {
        if (cfg == null) {
            return null;
        }
        String gwId = cfg.getGwId();
        return StringUtils.hasText(gwId) ? gwId : null;
    }

    private static String oldEquipIdToHdel(String oldEquipId, String newEquipId) {
        if (!StringUtils.hasText(oldEquipId)) {
            return "";
        }
        if (!StringUtils.hasText(newEquipId) || !oldEquipId.equals(newEquipId)) {
            return oldEquipId;
        }
        return "";
    }

    private Cache tenantCache(String tenantId) {
        Cache springCache = cacheManager.getCache(equipRtConfigCacheName(tenantId));
        if (springCache == null) {
            throw new BusinessException("缓存未初始化: " + equipRtConfigCacheName(tenantId));
        }
        return springCache;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public void addOrUpdate(String tenantId, String selfCode, EquipRealtimeConfig config) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(selfCode)) {
            return;
        }
        String kMain = configCacheRedisKey(tenantId, selfCode);
        String idHash = idToSnIndexKey(tenantId);
        if (config == null) {
            EquipRealtimeConfig old = get(tenantId, selfCode);
            String oldGw = resolveGwId(old);
            boolean sremGw = StringUtils.hasText(oldGw);
            String kGw = sremGw ? gwToSnSetKey(tenantId, oldGw) : kMain;
            String eqHdel = old != null && StringUtils.hasText(old.getEquipId()) ? old.getEquipId() : "";
            EquipRealtimeConfigRedisLua.executeRemove(
                    redisTemplate,
                    Arrays.asList(kMain, kGw, idHash),
                    selfCode,
                    sremGw,
                    eqHdel);
            return;
        }
        EquipRealtimeConfig old = get(tenantId, selfCode);
        String oldGw = resolveGwId(old);
        String newGw = resolveGwId(config);
        boolean sremOldGw = StringUtils.hasText(oldGw) && (!Objects.equals(oldGw, newGw) || !StringUtils.hasText(newGw));
        boolean saddNewGw = StringUtils.hasText(newGw);
        String kOldGw = sremOldGw ? gwToSnSetKey(tenantId, oldGw) : kMain;
        String kNewGw = saddNewGw ? gwToSnSetKey(tenantId, newGw) : kMain;

        String newEq = StringUtils.hasText(config.getEquipId()) ? config.getEquipId() : "";
        String oldEq = old != null && StringUtils.hasText(old.getEquipId()) ? old.getEquipId() : "";
        String oldEqHdel = oldEquipIdToHdel(oldEq, newEq);

        EquipRealtimeConfigRedisLua.executeUpsert(
                redisTemplate,
                Arrays.asList(kMain, kOldGw, kNewGw, idHash),
                config,
                EquipRealtimeConfigRedisLua.CACHE_TTL_SECONDS,
                selfCode,
                sremOldGw,
                saddNewGw,
                newEq,
                oldEqHdel);
    }

    @Override
    public void remove(String tenantId, String selfCode) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(selfCode)) {
            return;
        }
        addOrUpdate(tenantId, selfCode, null);
    }

    @Override
    public EquipRealtimeConfig get(String tenantId, String selfCode) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(selfCode)) {
            return null;
        }
        Cache.ValueWrapper w = tenantCache(tenantId).get(selfCode);
        return w == null ? null : cacheValueConverter.convert(w.get(), EquipRealtimeConfig.class);
    }

    @Override
    public EquipRealtimeConfig getByEquipId(String tenantId, String equipId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(equipId)) {
            return null;
        }
        Object sn = stringRedisTemplate.opsForHash().get(idToSnIndexKey(tenantId), equipId);
        if (!(sn instanceof String selfCode) || !StringUtils.hasText(selfCode)) {
            return null;
        }
        return get(tenantId, selfCode);
    }

    @Override
    public List<String> listSelfCodeByGwId(String tenantId, String gwId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(gwId)) {
            return Collections.emptyList();
        }
        Set<String> sns;
        try {
            sns = stringRedisTemplate.opsForSet().members(gwToSnSetKey(tenantId, gwId));
        } catch (Exception ex) {
            log.error("listSelfCodeByGwId SMEMBERS failed: tenantId={} gwId={}", tenantId, gwId, ex);
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(sns)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(sns);
    }

    @Override
    public void reload() {
        RLock lock = redissonClient.getLock(EQUIP_CONFIG_SYNC_LOCK_KEY);
        boolean locked;
        try {
            locked = lock.tryLock(0, -1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("reload equip config cache interrupted while acquiring Redisson lock");
            return;
        }
        if (!locked) {
            log.info("reload equip config cache skipped: lock busy ({})", EQUIP_CONFIG_SYNC_LOCK_KEY);
            return;
        }
        try {
            UserContext.defaultTenant();
            try {
                Set<String> tenantsCleared = new HashSet<>();
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
                    putReloadBatch(equipDtos, tenantsCleared);
                    if (equipDtos.size() < RELOAD_PAGE_SIZE) {
                        break;
                    }
                    pageNum++;
                }
            } catch (Exception e) {
                log.error("reload equip config cache failed: {}", e.getMessage(), e);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void putReloadBatch(List<EquipDto> equipDtos, Set<String> tenantsCleared) {
        for (EquipDto equipDto : equipDtos) {
            String tid = equipDto.getTenantId();
            if (tid != null && tenantsCleared.add(tid)) {
                try {
                    EquipRealtimeConfigRedisLua.executeClearTenantConfigKeys(
                            redisTemplate,
                            idToSnIndexKey(tid),
                            equipRtConfigCacheName(tid) + "::*",
                            gwToSnKeyPattern(tid));
                } catch (Exception ex) {
                    log.error("reload equip config: clear tenant redis failed tid={}", tid, ex);
                }
            }
            if (equipDto.getTenantId() == null || !StringUtils.hasText(equipDto.getSelfCode())) {
                continue;
            }
            EquipRealtimeConfig cfg = EquipRealtimeConfigFromDto.build(equipDto);
            addOrUpdate(equipDto.getTenantId(), equipDto.getSelfCode(), cfg);
        }
    }
}
