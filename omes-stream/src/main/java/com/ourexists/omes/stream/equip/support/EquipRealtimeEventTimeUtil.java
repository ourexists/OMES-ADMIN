package com.ourexists.omes.stream.equip.support;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;

import java.util.Date;

public final class EquipRealtimeEventTimeUtil {

    private EquipRealtimeEventTimeUtil() {
    }

    public static boolean isNewerOrSame(EquipRealtime candidate, EquipRealtime baseline) {
        return extractEventTimestamp(candidate) >= extractEventTimestamp(baseline);
    }

    private static long extractEventTimestamp(EquipRealtime realtime) {
        Date eventTime = realtime == null ? null : realtime.getTime();
        return eventTime == null ? 0L : eventTime.getTime();
    }
}
