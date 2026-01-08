package com.ourexists.mesedge.device.core;

import lombok.AccessLevel;
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

    @Setter(AccessLevel.NONE)
    private int onlineState = 0;

    @Setter(AccessLevel.NONE)
    private int runState = 0;

    @Setter(AccessLevel.NONE)
    private int alarmState = 0;

    private List<EquipAttrRealtime> equipAttrRealtimes;

    private String tenantId;

    private String workshopCode;

    private Date time;

    private Date onlineTime;

    private Date offlineTime;

    private Date runTime;

    private Date stopTime;

    private Date alarmTime;

    private Date alarmEndTime;

    public void online() {
        this.onlineState = 1;
        this.onlineTime = new Date();
    }


    public void offline() {
        this.onlineState = 0;
        this.offlineTime = new Date();
        if (this.equipRealtimeConfig != null) {
            this.equipAttrRealtimes = this.equipRealtimeConfig.getAttrs();
        } else {
            this.equipAttrRealtimes = null;
        }
    }

    public void run() {
        this.runState = 1;
        this.runTime = new Date();
    }


    public void stop() {
        this.runState = 0;
        this.stopTime = new Date();
    }

    public void alarm() {
        this.alarmState = 1;
        this.alarmTime = new Date();
    }

    public void resetAlarm() {
        this.alarmState = 0;
        this.alarmEndTime = new Date();
    }

}
