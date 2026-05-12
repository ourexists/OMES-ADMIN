package com.ourexists.omes.device.core.equip.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtime {

    private String id;

    private String name;

    private String selfCode;

    private EquipRealtimeConfig equipRealtimeConfig;

    private Integer onlineState = 0;

    /** 运行：-1 未知，0 停止，1 运行 */
    private Integer runState = -1;

    /** 报警：-1 未知，0 正常，1 报警 */
    private Integer alarmState = -1;

    private List<EquipAttrRealtime> equipAttrRealtimes;

    private List<EquipControlRealtime> equipControlRealtimes;

    private List<String> alarmTexts;

    private Integer alarmLevel;

    private String tenantId;

    private String workshopCode;

    private Date time;

    private Date onlineChangeTime;

    private Date runChangeTime;

    private Date alarmChangeTime;

    private Boolean onlineChange = false;
}
