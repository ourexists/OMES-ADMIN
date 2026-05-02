package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.model.EquipStateSnapshotDto;
import com.ourexists.omes.stream.equip.model.EquipStateSnapshotEvent;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Date;

public class EquipStateSnapshotProcessFunction extends KeyedProcessFunction<String, EquipRealtime, EquipStateSnapshotEvent> {
    private final long snapshotIntervalMs;
    private transient ValueState<EquipRealtime> latestState;
    private transient ValueState<Long> nextSnapshotTimerState;

    public EquipStateSnapshotProcessFunction(long snapshotIntervalMs) {
        this.snapshotIntervalMs = snapshotIntervalMs;
    }

    @Override
    public void open(Configuration parameters) {
        latestState = getRuntimeContext().getState(new ValueStateDescriptor<>("snapshot-latest-state", EquipRealtime.class));
        nextSnapshotTimerState = getRuntimeContext().getState(new ValueStateDescriptor<>("snapshot-next-timer", Long.class));
    }

    @Override
    public void processElement(EquipRealtime value, Context ctx, Collector<EquipStateSnapshotEvent> out) throws Exception {
        EquipRealtime current = latestState.value();
        if (shouldUpdateLatestState(current, value)) {
            latestState.update(value);
        }
        Long timer = nextSnapshotTimerState.value();
        if (timer == null) {
            long firstTs = ctx.timerService().currentProcessingTime() + snapshotIntervalMs;
            ctx.timerService().registerProcessingTimeTimer(firstTs);
            nextSnapshotTimerState.update(firstTs);
        }
    }

    private boolean shouldUpdateLatestState(EquipRealtime current, EquipRealtime incoming) {
        if (incoming == null) {
            return false;
        }
        if (current == null) {
            return true;
        }
        return EquipRealtimeEventTimeUtil.isNewerOrSame(incoming, current);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<EquipStateSnapshotEvent> out) throws Exception {
        EquipRealtime latest = latestState.value();
        if (latest == null) {
            nextSnapshotTimerState.clear();
            return;
        }
        EquipStateSnapshotDto snapshot = new EquipStateSnapshotDto()
                .setSn(latest.getSelfCode())
                .setRunState(latest.getRunState())
                .setAlarmState(latest.getAlarmState())
                .setOnlineState(latest.getOnlineState())
                .setTime(new Date(timestamp))
                .setTenantId(latest.getTenantId());
        out.collect(new EquipStateSnapshotEvent(snapshot));

        long nextTs = timestamp + snapshotIntervalMs;
        ctx.timerService().registerProcessingTimeTimer(nextTs);
        nextSnapshotTimerState.update(nextTs);
    }
}
