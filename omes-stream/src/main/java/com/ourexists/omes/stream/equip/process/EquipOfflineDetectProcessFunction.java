package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.beans.BeanUtils;

import java.util.Date;

public class EquipOfflineDetectProcessFunction extends KeyedProcessFunction<String, EquipRealtime, EquipRealtime> {
    private final long timeoutMs;
    private transient ValueState<EquipRealtime> latestEventState;
    private transient ValueState<Long> timeoutTimerState;

    public EquipOfflineDetectProcessFunction(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void open(Configuration parameters) {
        latestEventState = getRuntimeContext().getState(new ValueStateDescriptor<>("latest-event-state", EquipRealtime.class));
        timeoutTimerState = getRuntimeContext().getState(new ValueStateDescriptor<>("timeout-timer-state", Long.class));
    }

    @Override
    public void processElement(EquipRealtime value, Context ctx, Collector<EquipRealtime> out) throws Exception {
        latestEventState.update(value);
        Long oldTimer = timeoutTimerState.value();
        if (oldTimer != null) {
            ctx.timerService().deleteProcessingTimeTimer(oldTimer);
        }
        long timeoutTs = ctx.timerService().currentProcessingTime() + timeoutMs;
        ctx.timerService().registerProcessingTimeTimer(timeoutTs);
        timeoutTimerState.update(timeoutTs);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<EquipRealtime> out) throws Exception {
        EquipRealtime latestEvent = latestEventState.value();
        if (latestEvent == null) {
            return;
        }
        Long timer = timeoutTimerState.value();
        if (timer == null || timestamp != timer) {
            return;
        }
        EquipRealtime offlineTarget = new EquipRealtime();
        BeanUtils.copyProperties(latestEvent, offlineTarget);
        offlineTarget.offline();
        offlineTarget.setTime(new Date(timestamp));
        out.collect(offlineTarget);
        timeoutTimerState.clear();
    }
}
