package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipAttrRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.model.EquipAttrFluctuationWindowEvent;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class EquipAttrFluctuationWindowProcessFunction extends ProcessWindowFunction<EquipRealtime, EquipAttrFluctuationWindowEvent, String, TimeWindow> {
    private static final double MIN_BASELINE = 1e-6D;

    @Override
    public void process(String selfCode, Context context, Iterable<EquipRealtime> elements, Collector<EquipAttrFluctuationWindowEvent> out) {
        EquipRealtime latest = null;
        Map<String, Double> minValues = new HashMap<>();
        Map<String, Double> maxValues = new HashMap<>();
        for (EquipRealtime realtime : elements) {
            if (realtime == null) {
                continue;
            }
            if (latest == null || EquipRealtimeEventTimeUtil.isNewerOrSame(realtime, latest)) {
                latest = realtime;
            }
            if (realtime.getEquipAttrRealtimes() == null) {
                continue;
            }
            for (EquipAttrRealtime attr : realtime.getEquipAttrRealtimes()) {
                if (attr == null || StringUtils.isBlank(attr.getName()) || StringUtils.isBlank(attr.getValue())) {
                    continue;
                }
                if (Boolean.FALSE.equals(attr.getFluctuationEnabled())) {
                    continue;
                }
                Double numeric = tryParseDouble(attr.getValue());
                if (numeric == null) {
                    continue;
                }
                minValues.merge(attr.getName(), numeric, Math::min);
                maxValues.merge(attr.getName(), numeric, Math::max);
            }
        }
        if (latest == null || latest.getEquipAttrRealtimes() == null) {
            return;
        }
        for (EquipAttrRealtime attr : latest.getEquipAttrRealtimes()) {
            if (attr == null || StringUtils.isBlank(attr.getName())) {
                continue;
            }
            if (Boolean.FALSE.equals(attr.getFluctuationEnabled())) {
                continue;
            }
            Double min = minValues.get(attr.getName());
            Double max = maxValues.get(attr.getName());
            boolean exceeded = false;
            Double thresholdRatio = attr.getFluctuationThresholdRatio();
            Double minDelta = attr.getFluctuationMinDelta();
            Integer requiredWindowsRaw = attr.getFluctuationConsecutiveWindows();
            if (thresholdRatio == null || minDelta == null || requiredWindowsRaw == null) {
                continue;
            }
            int requiredWindows = Math.max(1, requiredWindowsRaw);
            if (min != null && max != null) {
                double delta = Math.abs(max - min);
                if (delta >= minDelta) {
                    double baseline = Math.max(Math.abs(min), MIN_BASELINE);
                    double ratio = delta / baseline;
                    exceeded = ratio >= thresholdRatio;
                }
            }
            out.collect(new EquipAttrFluctuationWindowEvent(
                    latest,
                    selfCode,
                    attr.getName(),
                    exceeded,
                    requiredWindows
            ));
        }
    }

    private Double tryParseDouble(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}
