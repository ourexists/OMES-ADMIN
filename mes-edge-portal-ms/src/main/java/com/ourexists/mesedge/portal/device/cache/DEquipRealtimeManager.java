package com.ourexists.mesedge.portal.device.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class DEquipRealtimeManager implements EquipRealtimeManager {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EquipFeign equipFeign;

    private static final String CACHE_NAME = "EquipRealtime";

    public Cache<Object, Object> nativeCache(String cacheName) {
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(cacheName);
        return springCache.getNativeCache();
    }


    @PostConstruct
    public void init() {
        UserContext.defaultTenant();
        EquipPageQuery query = new EquipPageQuery();
        query.setRequirePage(false);
        try {
            List<EquipDto> equipDtos = RemoteHandleUtils.getDataFormResponse(equipFeign.selectByPage(query));
            Map<String, Map<String, EquipRealtime>> equipRealtimeMap = new HashMap<>();
            for (EquipDto equipDto : equipDtos) {
                Map<String, EquipRealtime> r = equipRealtimeMap.get(equipDto.getTenantId());
                if (r == null) {
                    r = new HashMap<>();
                }
                EquipRealtime equipRealtime = new EquipRealtime();
                BeanUtils.copyProperties(equipDto, equipRealtime);
                r.put(equipDto.getSelfCode(), equipRealtime);
                equipRealtimeMap.put(equipDto.getTenantId(), r);
            }
            for (Map.Entry<String, Map<String, EquipRealtime>> entry : equipRealtimeMap.entrySet()) {
                reset(entry.getKey(), entry.getValue());
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }


    @Override
    public void reset(String tenantId, Map<String, EquipRealtime> equipRealtimeMap) {
        nativeCache(CACHE_NAME + "_" + tenantId).putAll(equipRealtimeMap);
    }

    @Override
    public void addOrUpdate(String tenantId, EquipRealtime equipRealtime) {
        nativeCache(CACHE_NAME + "_" + tenantId).put(equipRealtime.getSelfCode(), equipRealtime);
    }


    @Override
    public void remove(String tenantId, String sn) {
        nativeCache(CACHE_NAME + "_" + tenantId).invalidate(sn);
    }

    @Override
    public void clear(String tenantId) {
        nativeCache(CACHE_NAME + "_" + tenantId).cleanUp();
    }

    @Override
    public Map<String, EquipRealtime> getAll(String tenantId) {
        Map<String, EquipRealtime> r = new HashMap<>();
        ConcurrentMap<Object, Object> c = nativeCache(CACHE_NAME + "_" + tenantId).asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            r.put((String) entry.getKey(), (EquipRealtime) entry.getValue());
        }
        return r;
    }

    @Override
    public EquipRealtime get(String tenantId, String sn) {
        Object r = nativeCache(CACHE_NAME + "_" + tenantId).getIfPresent(sn);
        if (r == null) {
            return null;
        } else {
            return (EquipRealtime) r;
        }
    }
}
