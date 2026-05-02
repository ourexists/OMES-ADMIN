package com.ourexists.omes.stream.equip.model;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;

public class EquipAttrFluctuationWindowEvent {
    private final EquipRealtime latestRealtime;
    private final String selfCode;
    private final String attrName;
    private final boolean exceeded;
    private final int requiredWindows;

    public EquipAttrFluctuationWindowEvent(
            EquipRealtime latestRealtime,
            String selfCode,
            String attrName,
            boolean exceeded,
            int requiredWindows) {
        this.latestRealtime = latestRealtime;
        this.selfCode = selfCode;
        this.attrName = attrName;
        this.exceeded = exceeded;
        this.requiredWindows = requiredWindows;
    }

    public EquipRealtime getLatestRealtime() {
        return latestRealtime;
    }

    public String getSelfCode() {
        return selfCode;
    }

    public String getAttrName() {
        return attrName;
    }

    public boolean isExceeded() {
        return exceeded;
    }

    public int getRequiredWindows() {
        return requiredWindows;
    }
}
