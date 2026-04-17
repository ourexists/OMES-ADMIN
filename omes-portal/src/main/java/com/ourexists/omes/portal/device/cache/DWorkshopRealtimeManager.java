package com.ourexists.omes.portal.device.cache;

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
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Component
public class DWorkshopRealtimeManager implements WorkshopRealtimeManager {

    private final CacheManager cacheManager;
    private final StringRedisTemplate stringRedisTemplate;

    private final WorkshopFeign workshopFeign;

    private final TenantFeign tenantFeign;

    private static final String CACHE_NAME = "WORKSHOP_REALTIME_";

    public DWorkshopRealtimeManager(CacheManager cacheManager,
                                    StringRedisTemplate stringRedisTemplate,
                                    WorkshopFeign workshopFeign,
                                    TenantFeign tenantFeign) {
        this.cacheManager = cacheManager;
        this.stringRedisTemplate = stringRedisTemplate;
        this.workshopFeign = workshopFeign;
        this.tenantFeign = tenantFeign;
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
        Cache springCache = cacheManager.getCache(CACHE_NAME + tenantId);
        if (springCache == null) {
            throw new IllegalStateException("[WorkshopRealtime] cache not found");
        }
        return springCache;
    }

    private Map<String, WorkshopRealtime> getAllByTenant(String tenantId) {
        Cache cache = cacheManager.getCache(CACHE_NAME + tenantId);
        if (cache == null) {
            return new HashMap<>();
        }
        String keyPrefix = CACHE_NAME + tenantId + "::";
        Set<String> keys = stringRedisTemplate.keys(keyPrefix + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>();
        }
        Map<String, WorkshopRealtime> result = new HashMap<>(keys.size());
        for (String redisKey : keys) {
            String cacheKey = redisKey.substring(keyPrefix.length());
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null && valueWrapper.get() instanceof WorkshopRealtime workshopRealtime) {
                result.put(cacheKey, workshopRealtime);
            }
        }
        return result;
    }

    @Override
    public void realtimeHandle(List<WorkshopRealtime> targets) {
        Cache cache = tenantCache();
        for (WorkshopRealtime target : targets) {
            if (!StringUtils.hasText(target.getId())) {
                throw new IllegalArgumentException("[WorkshopRealtime] id is required");
            }
            WorkshopRealtime source = get(target.getId());
            if (source == null) {
                continue;
            }
            source.setAttrsRealtime(target.getAttrsRealtime());
            source.setTime(new Date());
            cache.put(source.getId(), source);
        }
    }

    @Override
    public void build(WorkshopRealtime realtime) {
        if (!StringUtils.hasText(realtime.getId())) {
            throw new IllegalArgumentException("[WorkshopRealtime] id is required");
        }
        if (realtime.getAttrsRealtime() == null) {
            if (realtime.getConfig() != null && realtime.getConfig().getAttrs() != null) {
                realtime.setAttrsRealtime(realtime.getConfig().getAttrs());
            }
        }
        tenantCache().put(realtime.getId(), realtime);
    }

    public void build(List<WorkshopRealtime> realtimes) {
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
            cache.put(realtime.getId(), realtime);
        }
    }

    @Override
    public void remove(String id) {
        tenantCache().evict(id);
    }

    @Override
    public void removeBatch(List<String> ids) {
        Cache cache = tenantCache();
        ids.forEach(cache::evict);
    }

    @Override
    public void clear() {
        tenantCache().clear();
    }

    @Override
    public Map<String, WorkshopRealtime> getAll() {
        return getAllByTenant(UserContext.getTenant().getTenantId());
    }

    @Override
    public WorkshopRealtime get(String id) {
        Cache.ValueWrapper valueWrapper = tenantCache().get(id);
        return valueWrapper == null ? null : (WorkshopRealtime) valueWrapper.get();
    }

    @Override
    public void reload() {
        try {
            List<WorkshopConfigCollectDto> dtos = RemoteHandleUtils.getDataFormResponse(workshopFeign.queryAllConfigCollect());
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
            this.build(realtimes);
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
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
}
