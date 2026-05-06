package com.ourexists.omes.device.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stable fingerprint for alarm de-duplication (notify / DB insert). Same text set and level produce the same string
 * regardless of original list order; {@code alarmLevel} distinguishes same text at different levels.
 */
public final class EquipAlarmFingerprint {

    private EquipAlarmFingerprint() {}

    /**
     * Same key as persisted {@link EquipRecordAlarmDto#getSn()} (device self code from stream).
     */
    public static String ofSn(String sn, Integer level, List<String> alarmTexts) {
        return of(sn == null ? "" : sn, level, alarmTexts);
    }

    public static String of(String keyPart, Integer level, List<String> alarmTexts) {
        String kp = keyPart == null ? "" : keyPart;
        String levelPart = level == null ? "null" : level.toString();
        if (alarmTexts == null || alarmTexts.isEmpty()) {
            return kp + "|DEFAULT|" + levelPart;
        }
        List<String> sorted = new ArrayList<>();
        for (String s : alarmTexts) {
            if (s != null && !s.isBlank()) {
                sorted.add(s.trim());
            }
        }
        if (sorted.isEmpty()) {
            return kp + "|DEFAULT|" + levelPart;
        }
        Collections.sort(sorted);
        return kp + "|" + levelPart + "|" + String.join("\u0001", sorted);
    }

    /** {@code reason} as persisted on alarm rows (comma-separated). */
    public static String fromReasonCsv(String keyPart, Integer level, String reasonCsv) {
        if (reasonCsv == null || reasonCsv.isBlank()) {
            return of(keyPart, level, null);
        }
        String[] parts = reasonCsv.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                list.add(p.trim());
            }
        }
        return of(keyPart, level, list.isEmpty() ? null : list);
    }
}
