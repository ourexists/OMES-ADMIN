package com.ourexists.mesedge.portal.device.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.DateUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeConfig;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import com.ourexists.mesedge.message.enums.MessageSourceEnum;
import com.ourexists.mesedge.message.enums.MessageTypeEnum;
import com.ourexists.mesedge.message.feign.NotifyFeign;
import com.ourexists.mesedge.message.model.NotifyDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class DEquipRealtimeManager implements EquipRealtimeManager {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EquipFeign equipFeign;

    @Autowired
    private WorkshopFeign workshopFeign;

    @Autowired
    private NotifyFeign notifyFeign;

    private static final String CACHE_NAME = "EquipRealtime";

    public Cache<Object, Object> nativeCache(String cacheName) {
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(cacheName);
        return springCache.getNativeCache();
    }


    @PostConstruct
    public void init() {
        reload();
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
    public void removeBatch(String tenantId, List<String> ids) {
        Map<Object, Object> map = nativeCache(CACHE_NAME + "_" + tenantId).asMap();
        List<String> sns = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            EquipRealtime e = (EquipRealtime) entry.getValue();
            if (ids.contains(e.getId())) {
                sns.add(e.getSelfCode());
            }
        }
        nativeCache(CACHE_NAME + "_" + tenantId).invalidateAll(sns);
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

    @Override
    public EquipRealtime getById(String tenantId, String id) {
        ConcurrentMap<Object, Object> c = nativeCache(CACHE_NAME + "_" + tenantId).asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            EquipRealtime equipRealtime = (EquipRealtime) entry.getValue();
            if (equipRealtime.getId().equals(id)) {
                return equipRealtime;
            }
        }
        return null;
    }

    @Override
    public void reload() {
        UserContext.defaultTenant();
        EquipPageQuery query = new EquipPageQuery();
        query.setRequirePage(false);
        query.setQueryConfig(true);
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

                if (equipDto.getConfig() != null && equipDto.getConfig().getConfig() != null) {
                    EquipRealtimeConfig equipRealtimeConfig = new EquipRealtimeConfig();
                    BeanUtils.copyProperties(equipDto.getConfig().getConfig(), equipRealtimeConfig);
                    if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getAttrs())) {
                        List<EquipAttrRealtime> attrs = new ArrayList<>();
                        equipDto.getConfig().getConfig().getAttrs().forEach(attr -> {
                            EquipAttrRealtime equipAttrRealtime = new EquipAttrRealtime();
                            BeanUtils.copyProperties(attr, equipAttrRealtime);
                            attrs.add(equipAttrRealtime);
                        });
                        equipRealtimeConfig.setAttrs(attrs);
                    }
                    equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
                    equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
                }
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
    public void change(EquipRealtime source, EquipRealtime target) {
        if (target.getAlarmState() == 1 && source.getAlarmState() != target.getAlarmState()) {
            try {
                WorkshopTreeNode workshopTreeNode = RemoteHandleUtils.getDataFormResponse(workshopFeign.selectByCode(source.getWorkshopCode()));
                String workName = workshopTreeNode == null ? "" : workshopTreeNode.getName();
                List<String> platforms = new ArrayList<>();
                platforms.add("MES APP");
                NotifyDto dto = new NotifyDto()
                        .setStep(0)
                        .setContext("[" + DateUtil.dateFormat(new Date()) + "]\r <" + workName + " - " + target.getName() + "> 设备产生报警")
                        .setTitle("【" + target.getName() + "】异常报警")
                        .setSource(MessageSourceEnum.Equip.name())
                        .setSourceId(source.getId())
                        .setPlatforms(platforms)
                        .setType(MessageTypeEnum.ALARM.getCode());
                notifyFeign.createAndStart(dto);
            } catch (EraCommonException e) {
                log.error(e.getMessage(), e);
            }
        }

    }
}
