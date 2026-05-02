package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.model.EquipAttrFluctuationWindowEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class EquipAttrFluctuationConsecutiveProcessFunction extends KeyedProcessFunction<String, EquipAttrFluctuationWindowEvent, EquipRealtime> {
    private transient ValueState<Integer> consecutiveState;

    @Override
    public void open(Configuration parameters) {
        consecutiveState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("equip-attr-fluctuation-consecutive-count", Integer.class)
        );
    }

    @Override
    public void processElement(EquipAttrFluctuationWindowEvent event, Context ctx, Collector<EquipRealtime> out) throws Exception {
        if (event == null) {
            return;
        }
        Integer current = consecutiveState.value();
        int next = event.isExceeded() ? (current == null ? 1 : current + 1) : 0;
        consecutiveState.update(next);
        if (!event.isExceeded() || next < event.getRequiredWindows()) {
            return;
        }
        EquipRealtime latest = event.getLatestRealtime();
        if (latest == null) {
            return;
        }
        EquipRealtime alarmRealtime = new EquipRealtime();
        BeanUtils.copyProperties(latest, alarmRealtime);
        List<String> mergedAlarmTexts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(latest.getAlarmTexts())) {
            mergedAlarmTexts.addAll(latest.getAlarmTexts());
        }
        mergedAlarmTexts.add("【" + event.getAttrName() + "】数据波动异常，请及时排查");
        alarmRealtime.setAlarmTexts(mergedAlarmTexts);
        alarmRealtime.alarm();
        out.collect(alarmRealtime);
    }
}
