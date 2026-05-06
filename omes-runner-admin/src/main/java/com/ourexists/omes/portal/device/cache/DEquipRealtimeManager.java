package com.ourexists.omes.portal.device.cache;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.*;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipPageQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DEquipRealtimeManager implements EquipRealtimeManager {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EquipFeign equipFeign;

    @Autowired
    private RedisCacheValueConverter cacheValueConverter;

    private static final String CACHE_NAME = "equip_realtime_";

    private Cache tenantCache() {
        String tenantId = UserContext.getTenant().getTenantId();
        Cache springCache = cacheManager.getCache(CACHE_NAME + tenantId);
        if (springCache == null) {
            throw new BusinessException("缓存未初始化: " + CACHE_NAME + tenantId);
        }
        return springCache;
    }

    private Cache tenantCache(String tenantId) {
        Cache springCache = cacheManager.getCache(CACHE_NAME + tenantId);
        if (springCache == null) {
            throw new BusinessException("缓存未初始化: " + CACHE_NAME + tenantId);
        }
        return springCache;
    }

    private Map<String, EquipRealtime> getAll(String tenantId) {
        Cache cache = tenantCache(tenantId);
        String keyPrefix = CACHE_NAME + tenantId + "::";
        Set<String> keys = stringRedisTemplate.keys(keyPrefix + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>();
        }
        Map<String, EquipRealtime> result = new HashMap<>(keys.size());
        for (String redisKey : keys) {
            String cacheKey = redisKey.substring(keyPrefix.length());
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            EquipRealtime equipRealtime = valueWrapper == null ? null : cacheValueConverter.convert(valueWrapper.get(), EquipRealtime.class);
            if (equipRealtime != null) {
                result.put(cacheKey, equipRealtime);
            }
        }
        return result;
    }

    @PostConstruct
    public void init() {
        reload();
    }


    @Override
    public void addOrUpdate(EquipRealtime equipRealtime) {
        tenantCache().put(equipRealtime.getSelfCode(), equipRealtime);
    }

    @Override
    public void remove(String sn) {
        tenantCache().evict(sn);
    }

    @Override
    public void removeBatch(List<String> ids) {
        Cache cache = tenantCache();
        Map<String, EquipRealtime> all = getAll(UserContext.getTenant().getTenantId());
        List<String> sns = all.values().stream().filter(e -> ids.contains(e.getId())).map(EquipRealtime::getSelfCode).collect(Collectors.toList());
        sns.forEach(cache::evict);
    }

    @Override
    public void clear() {
        tenantCache().clear();
    }

    @Override
    public Map<String, EquipRealtime> getAll() {
        return getAll(UserContext.getTenant().getTenantId());
    }

    @Override
    public EquipRealtime get(String sn) {
        Cache.ValueWrapper valueWrapper = tenantCache().get(sn);
        return valueWrapper == null ? null : cacheValueConverter.convert(valueWrapper.get(), EquipRealtime.class);
    }

    @Override
    public EquipRealtime getById(String id) {
        Map<String, EquipRealtime> c = getAll(UserContext.getTenant().getTenantId());
        for (Map.Entry<String, EquipRealtime> entry : c.entrySet()) {
            EquipRealtime equipRealtime = entry.getValue();
            if (id.equals(equipRealtime.getId())) {
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
                    if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getAlarms())) {
                        List<EquipAlarmRealtime> alarms = new ArrayList<>();
                        equipDto.getConfig().getConfig().getAlarms().forEach(alarm -> {
                            EquipAlarmRealtime equipAlarmRealtime = new EquipAlarmRealtime();
                            BeanUtils.copyProperties(alarm, equipAlarmRealtime);
                            alarms.add(equipAlarmRealtime);
                        });
                        equipRealtimeConfig.setAlarms(alarms);
                    }
                    if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getControls())) {
                        List<EquipControlRealtime> controls = new ArrayList<>();
                        equipDto.getConfig().getConfig().getControls().forEach(ctrl -> {
                            EquipControlRealtime equipControlRealtime = new EquipControlRealtime();
                            BeanUtils.copyProperties(ctrl, equipControlRealtime);
                            controls.add(equipControlRealtime);
                        });
                        equipRealtimeConfig.setControls(controls);
                    }
                    equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
                    equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
                    equipRealtime.setEquipControlRealtimes(equipRealtimeConfig.getControls());
                    Date currentDate = new Date();
                    equipRealtime.setAlarmChangeTime(currentDate);
                    equipRealtime.setRunChangeTime(currentDate);
                    equipRealtime.setOnlineChangeTime(currentDate);
                }
                r.put(equipDto.getSelfCode(), equipRealtime);
                equipRealtimeMap.put(equipDto.getTenantId(), r);
            }
            for (Map.Entry<String, Map<String, EquipRealtime>> entry : equipRealtimeMap.entrySet()) {
                Cache cache = tenantCache(entry.getKey());
                entry.getValue().forEach(cache::put);
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }
}
