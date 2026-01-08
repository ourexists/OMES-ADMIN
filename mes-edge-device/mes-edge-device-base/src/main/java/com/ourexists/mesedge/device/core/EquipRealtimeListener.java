package com.ourexists.mesedge.device.core;

public interface EquipRealtimeListener {

    void runListener(EquipRealtime equipRealtime);

    void stopListener(EquipRealtime equipRealtime);

    void onlineListener(EquipRealtime equipRealtime);

    void offlineListener(EquipRealtime equipRealtime);

    void alarmListener(EquipRealtime equipRealtime);

    void alarmResetListener(EquipRealtime equipRealtime);
}
