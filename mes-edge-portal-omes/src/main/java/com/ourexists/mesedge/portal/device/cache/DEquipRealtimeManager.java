package com.ourexists.mesedge.portal.device.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.equip.cache.*;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.feign.EquipRecordAlarmFeign;
import com.ourexists.mesedge.device.feign.EquipRecordOnlineFeign;
import com.ourexists.mesedge.device.feign.EquipRecordRunFeign;
import com.ourexists.mesedge.device.model.*;
import com.ourexists.mesedge.message.enums.MessageSourceEnum;
import com.ourexists.mesedge.message.enums.MessageTypeEnum;
import com.ourexists.mesedge.message.feign.NotifyFeign;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.ucenter.feign.TenantFeign;
import com.ourexists.mesedge.ucenter.tenant.TenantVo;
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
    private NotifyFeign notifyFeign;

    @Autowired
    private TenantFeign tenantFeign;

    @Autowired
    private EquipRecordRunFeign equipRecordRunFeign;

    @Autowired
    private EquipRecordOnlineFeign equipRecordOnlineFeign;

    @Autowired
    private EquipRecordAlarmFeign equipRecordAlarmFeign;

    private static final String CACHE_NAME = "equip_realtime_";

    public Cache<Object, Object> nativeCache() {
        String tenantId = UserContext.getTenant().getTenantId();
        CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(CACHE_NAME + tenantId);
        return springCache.getNativeCache();
    }

    @PostConstruct
    public void init() {
        try {
            UserContext.defaultTenant();
            UserContext.getTenant().setSkipMain(false);
            List<TenantVo> tenantVos = RemoteHandleUtils.getDataFormResponse(tenantFeign.all());
            for (TenantVo tenantVo : tenantVos) {
                UserContext.getTenant().setTenantId(tenantVo.getTenantCode());
                reload();
            }
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void realtimeHandle(List<EquipRealtime> targets) {
        Map<Object, Object> map = nativeCache().asMap();
        List<EquipRealtime> sources = new ArrayList<>();
        for (EquipRealtime target : targets) {
            sources.add((EquipRealtime) map.get(target.getSelfCode()));
        }
        changeListener(sources, targets);
        for (EquipRealtime target : targets) {
            map.put(target.getSelfCode(), target);
        }
        nativeCache().putAll(map);
    }

    @Override
    public void addOrUpdate(EquipRealtime equipRealtime) {
        nativeCache().put(equipRealtime.getSelfCode(), equipRealtime);
    }


    @Override
    public void remove(String sn) {
        nativeCache().invalidate(sn);
    }

    @Override
    public void removeBatch(List<String> ids) {
        Map<Object, Object> map = nativeCache().asMap();
        List<String> sns = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            EquipRealtime e = (EquipRealtime) entry.getValue();
            if (ids.contains(e.getId())) {
                sns.add(e.getSelfCode());
            }
        }
        nativeCache().invalidateAll(sns);
    }

    @Override
    public void clear() {
        nativeCache().cleanUp();
    }

    @Override
    public Map<String, EquipRealtime> getAll() {
        Map<String, EquipRealtime> r = new HashMap<>();
        ConcurrentMap<Object, Object> c = nativeCache().asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            r.put((String) entry.getKey(), (EquipRealtime) entry.getValue());
        }
        return r;
    }

    @Override
    public EquipRealtime get(String sn) {
        Object r = nativeCache().getIfPresent(sn);
        if (r == null) {
            return null;
        } else {
            return (EquipRealtime) r;
        }
    }

    @Override
    public EquipRealtime getById(String id) {
        ConcurrentMap<Object, Object> c = nativeCache().asMap();
        for (Map.Entry<Object, Object> entry : c.entrySet()) {
            EquipRealtime equipRealtime = (EquipRealtime) entry.getValue();
            if (id.equals(equipRealtime.getId())) {
                return equipRealtime;
            }
        }
        return null;
    }

    @Override
    public void reload() {
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
                    equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
                    equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
                    Date currentDate = new Date();
                    equipRealtime.setAlarmChangeTime(currentDate);
                    equipRealtime.setRunChangeTime(currentDate);
                    equipRealtime.setOnlineChangeTime(currentDate);
                }
                r.put(equipDto.getSelfCode(), equipRealtime);
                equipRealtimeMap.put(equipDto.getTenantId(), r);
            }
            for (Map.Entry<String, Map<String, EquipRealtime>> entry : equipRealtimeMap.entrySet()) {
                nativeCache().putAll(entry.getValue());
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void changeListener(Collection<EquipRealtime> sources, Collection<EquipRealtime> targets) {
        List<EquipRecordAlarmDto> r = new ArrayList<>();
        List<EquipRecordRunDto> r1 = new ArrayList<>();
        List<EquipRecordOnlineDto> r2 = new ArrayList<>();
        for (EquipRealtime source : sources) {
            for (EquipRealtime target : targets) {
                if (source.getId().equals(target.getId())) {
                    if (!source.getAlarmState().equals(target.getAlarmState())) {
                        EquipRecordAlarmDto dto = new EquipRecordAlarmDto()
                                .setSn(source.getSelfCode())
                                .setState(target.getAlarmState())
                                .setStartTime(new Date())
                                .setTenantId(source.getTenantId());
                        r.add(dto);

                        if (target.getAlarmState() == 1) {
                            List<String> platforms = new ArrayList<>();
                            platforms.add("mes-app");
                            platforms.add("mes-edge");
                            StringBuilder context = new StringBuilder();
                            if (!CollectionUtils.isEmpty(target.getAlarmTexts())) {
                                for (String alarmText : target.getAlarmTexts()) {
                                    context.append(alarmText).append("\r\n");
                                }
                            } else {
                                context.append("设备报警");
                            }
                            NotifyDto notifyDto = new NotifyDto()
                                    .setStep(0)
                                    .setContext(context.toString())
                                    .setTitle("【" + target.getName() + "】异常报警")
                                    .setSource(MessageSourceEnum.Equip.name())
                                    .setSourceId(source.getId())
                                    .setPlatforms(platforms)
                                    .setType(MessageTypeEnum.ALARM.getCode());
                            notifyFeign.createAndStart(notifyDto);

                        }
                    }
                    if (!source.getRunState().equals(target.getRunState())) {
                        EquipRecordRunDto dto = new EquipRecordRunDto()
                                .setSn(source.getSelfCode())
                                .setState(target.getRunState())
                                .setStartTime(new Date())
                                .setTenantId(source.getTenantId());
                        r1.add(dto);
                    }

                    if (!source.getOnlineState().equals(target.getOnlineState())) {
                        EquipRecordOnlineDto dto = new EquipRecordOnlineDto()
                                .setSn(source.getSelfCode())
                                .setState(target.getOnlineState())
                                .setStartTime(new Date())
                                .setTenantId(source.getTenantId());
                        r2.add(dto);
                    }
                }
            }
        }
        try {
            if (!CollectionUtils.isEmpty(r)) {
                RemoteHandleUtils.getDataFormResponse(equipRecordAlarmFeign.addBatch(r));
            }
            if (!CollectionUtils.isEmpty(r1)) {
                RemoteHandleUtils.getDataFormResponse(equipRecordRunFeign.addBatch(r1));
            }
            if (!CollectionUtils.isEmpty(r2)) {
                RemoteHandleUtils.getDataFormResponse(equipRecordOnlineFeign.addBatch(r2));
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
