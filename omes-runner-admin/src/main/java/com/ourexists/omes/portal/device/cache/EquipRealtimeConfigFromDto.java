package com.ourexists.omes.portal.device.cache;

import com.ourexists.omes.device.core.equip.cache.*;
import com.ourexists.omes.device.model.EquipDto;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从 {@link EquipDto} 构建 {@link EquipRealtimeConfig}，供实时缓存与配置缓存各自 reload 复用（二者管理器互不依赖）。
 */
final class EquipRealtimeConfigFromDto {

    private EquipRealtimeConfigFromDto() {
    }

    static EquipRealtimeConfig build(EquipDto equipDto) {
        if (equipDto.getConfig() == null || equipDto.getConfig().getConfig() == null) {
            return null;
        }
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
        if (StringUtils.hasText(equipDto.getId())) {
            equipRealtimeConfig.setEquipId(equipDto.getId());
        }
        return equipRealtimeConfig;
    }
}
