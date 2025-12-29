package com.ourexists.mesedge.portal.device.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
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

import java.util.ArrayList;
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
        EquipPageQuery query = new EquipPageQuery();
        query.setRequirePage(false);
        try {
            List<EquipDto> equipDtos = RemoteHandleUtils.getDataFormResponse(equipFeign.selectByPage(query));
            Map<String, List<EquipRealtime>> equipRealtimeMap = new HashMap<>();
            for (EquipDto equipDto : equipDtos) {
                List<EquipRealtime> r = equipRealtimeMap.get(equipDto.getWorkshopCode());
                if (r == null) {
                    r = new ArrayList<>();
                }
                EquipRealtime equipRealtime = new EquipRealtime();
                BeanUtils.copyProperties(equipDto, equipRealtime);
                r.add(equipRealtime);
                equipRealtimeMap.put(equipDto.getWorkshopCode(), r);
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }


    @Override
    public void reset(String tenantId, Map<String, List<EquipRealtime>> equipRealtimeMap) {
        nativeCache(CACHE_NAME + "_" + tenantId).putAll(equipRealtimeMap);
    }

    @Override
    public void addOrUpdate(String tenantId, EquipRealtime equipRealtime) {
        Object r = nativeCache(CACHE_NAME + "_" + tenantId).getIfPresent(equipRealtime.getWorkshopCode());
        List<EquipRealtime> rs;
        if (r == null) {
            rs = new ArrayList<>();
            rs.add(equipRealtime);
        } else {
            rs = (List<EquipRealtime>) r;
            boolean isUp = false;
            for (EquipRealtime realtime : rs) {
                if (realtime.getSelfCode().equals(equipRealtime.getSelfCode())) {
                    BeanUtils.copyProperties(equipRealtime, realtime);
                }
            }
            if (!isUp) {
                rs.add(equipRealtime);
            }
        }
        nativeCache(CACHE_NAME + "_" + tenantId).put(equipRealtime.getWorkshopCode(), rs);
    }


    @Override
    public void remove(String tenantId, String sn) {
        Map<String, List<EquipRealtime>> m = getAll(tenantId);
        for (List<EquipRealtime> value : m.values()) {
            for (EquipRealtime realtime : value) {
                if (realtime.getSelfCode().equals(sn)) {
                    value.remove(realtime);
                    return;
                }
            }
        }
    }

    @Override
    public void removeById(String tenantId, String equipId) {
        Map<String, List<EquipRealtime>> m = getAll(tenantId);
        for (List<EquipRealtime> value : m.values()) {
            for (EquipRealtime realtime : value) {
                if (realtime.getId().equals(equipId)) {
                    value.remove(realtime);
                    return;
                }
            }
        }
    }

    @Override
    public void removeByIds(String tenantId, List<String> equipIds) {
        Map<String, List<EquipRealtime>> m = getAll(tenantId);
        for (List<EquipRealtime> value : m.values()) {
            value.removeIf(realtime -> equipIds.contains(realtime.getId()));
        }
    }

    @Override
    public void clear(String tenantId) {
        nativeCache(CACHE_NAME + "_" + tenantId).cleanUp();
    }

    @Override
    public Map<String, List<EquipRealtime>> getAll(String tenantId) {
        Map<String, List<EquipRealtime>> r = new HashMap<>();
        ConcurrentMap<Object, Object> c = nativeCache(CACHE_NAME + "_" + tenantId).asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            r.put(entry.getKey().toString(), (List<EquipRealtime>) entry.getValue());
        }
        return r;
    }

    @Override
    public List<EquipRealtime> getAll(String tenantId, String workshopCode) {
        Object c = nativeCache(CACHE_NAME + "_" + tenantId).getIfPresent(workshopCode);
        return (List<EquipRealtime>) c;
    }

    @Override
    public EquipRealtime get(String tenantId, String sn) {
        Map<String, List<EquipRealtime>> m = getAll(tenantId);
        for (List<EquipRealtime> value : m.values()) {
            for (EquipRealtime realtime : value) {
                if (realtime.getSelfCode().equals(sn)) {
                    return realtime;
                }
            }
        }
        return null;
    }
}
