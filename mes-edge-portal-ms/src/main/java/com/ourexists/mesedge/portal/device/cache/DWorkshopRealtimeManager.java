package com.ourexists.mesedge.portal.device.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeConfig;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectAttr;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDetail;
import com.ourexists.mesedge.device.model.WorkshopConfigCollectDto;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class DWorkshopRealtimeManager implements WorkshopRealtimeManager {

    private final CacheManager cacheManager;

    private final WorkshopFeign workshopFeign;

    private final TenantFeign tenantFeign;

    private static final String CACHE_NAME = "WORKSHOP_REALTIME_";

    public DWorkshopRealtimeManager(CacheManager cacheManager, WorkshopFeign workshopFeign, TenantFeign tenantFeign) {
        this.cacheManager = cacheManager;
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

    private Cache<Object, Object> nativeCache() {
        String tenantId = UserContext.getTenant().getTenantId();
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(CACHE_NAME + tenantId);
        if (springCache == null) {
            throw new IllegalStateException("[WorkshopRealtime] cache not found");
        }
        return springCache.getNativeCache();
    }

    @Override
    public void realtimeHandle(List<WorkshopRealtime> targets) {
        Map<Object, Object> map = new HashMap<>();
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
            map.put(source.getId(), source);
        }
        if (!CollectionUtils.isEmpty(map)) {
            nativeCache().putAll(map);
        }
    }

    @Override
    public void build(WorkshopRealtime realtime) {
        if (!StringUtils.hasText(realtime.getId())) {
            throw new IllegalArgumentException("[WorkshopRealtime] id is required");
        }
        nativeCache().put(realtime.getId(), realtime);
    }

    public void build(List<WorkshopRealtime> realtimes) {
        Map<Object, Object> map = new HashMap<>();
        for (WorkshopRealtime realtime : realtimes) {
            if (!StringUtils.hasText(realtime.getId())) {
                throw new IllegalArgumentException("[WorkshopRealtime] id is required");
            }
            map.put(realtime.getId(), realtime);
        }
        nativeCache().putAll(map);
    }

    @Override
    public void remove(String id) {
        nativeCache().invalidate(id);
    }

    @Override
    public void removeBatch(List<String> ids) {
        nativeCache().invalidateAll(ids);
    }

    @Override
    public void clear() {
        nativeCache().cleanUp();
    }

    @Override
    public Map<String, WorkshopRealtime> getAll() {
        Map<String, WorkshopRealtime> r = new HashMap<>();
        ConcurrentMap<Object, Object> c = nativeCache().asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            r.put((String) entry.getKey(), (WorkshopRealtime) entry.getValue());
        }
        return r;
    }

    @Override
    public WorkshopRealtime get(String id) {
        return (WorkshopRealtime) nativeCache().getIfPresent(id);
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
                if (!CollectionUtils.isEmpty(config.getAttrs())) {
                    List<WorkshopRealtimeCollect> attrs = new ArrayList<>();
                    for (WorkshopConfigCollectAttr attr : config.getAttrs()) {
                        WorkshopRealtimeCollect realtimeCollect = new WorkshopRealtimeCollect();
                        BeanUtils.copyProperties(attr, realtimeCollect);
                        attrs.add(realtimeCollect);
                    }
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
}
