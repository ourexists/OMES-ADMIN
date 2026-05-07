package com.ourexists.omes.device.core.equip.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtimeConfig {

    /** 设备主键，用于配置侧 id→selfCode 索引（与实时缓存 id 索引语义一致） */
    private String equipId;

    private String gwId;

    private String runMap;

    private List<EquipAttrRealtime> attrs;

    private List<EquipAlarmRealtime> alarms;

    private List<EquipControlRealtime> controls;
}
