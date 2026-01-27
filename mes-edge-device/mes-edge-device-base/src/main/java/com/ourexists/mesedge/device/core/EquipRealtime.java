package com.ourexists.mesedge.device.core;

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

    private Integer runState = 0;

    private Integer alarmState = 0;

    private List<EquipAttrRealtime> equipAttrRealtimes;

    private List<String> alarmTexts;

    private String tenantId;

    private String workshopCode;

    private Date time;

    private Date onlineChangeTime;

    private Date runChangeTime;

    private Date alarmChangeTime;


    public void online() {
        if (this.onlineState != 1) {
            this.onlineChangeTime = new Date();
            this.onlineState = 1;
        }
    }


    public void offline() {
        if (this.onlineState != 0) {
            this.onlineState = 0;
            this.onlineChangeTime = new Date();
            if (this.equipRealtimeConfig != null) {
                this.equipAttrRealtimes = this.equipRealtimeConfig.getAttrs();
            } else {
                this.equipAttrRealtimes = null;
            }
        }
    }

    public void run() {
        if (this.runState != 1) {
            this.runState = 1;
            this.runChangeTime = new Date();
        }
    }


    public void stop() {
        if (this.runState != 0) {
            this.runState = 0;
            this.runChangeTime = new Date();
        }
    }

    public void alarm() {
        if (this.alarmState != 1) {
            this.alarmState = 1;
            this.alarmChangeTime = new Date();
        }
    }

    public void resetAlarm() {
        if (this.alarmState != 0) {
            this.alarmState = 0;
            this.alarmChangeTime = new Date();
        }
    }

}
