package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.model.EquipRealtimeChangeEvent;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Objects;
import java.util.UUID;

public class EquipRealtimeChangeDetectProcessFunction extends KeyedProcessFunction<String, EquipRealtime, EquipRealtimeChangeEvent> {
    private transient ValueState<EquipRealtime> previousState;
    private transient ValueState<String> openAlarmSegmentEventIdState;
    private transient ValueState<String> openRunSegmentEventIdState;
    private transient ValueState<String> openOnlineSegmentEventIdState;

    @Override
    public void open(Configuration parameters) {
        previousState = getRuntimeContext().getState(new ValueStateDescriptor<>("equip-realtime-change-previous-state", EquipRealtime.class));
        openAlarmSegmentEventIdState =
                getRuntimeContext().getState(new ValueStateDescriptor<>("equip-realtime-open-alarm-segment-event-id", String.class));
        openRunSegmentEventIdState =
                getRuntimeContext().getState(new ValueStateDescriptor<>("equip-realtime-open-run-segment-event-id", String.class));
        openOnlineSegmentEventIdState =
                getRuntimeContext().getState(new ValueStateDescriptor<>("equip-realtime-open-online-segment-event-id", String.class));
    }

    @Override
    public void processElement(EquipRealtime value, Context ctx, Collector<EquipRealtimeChangeEvent> out) throws Exception {
        if (!shouldProcess(value)) {
            return;
        }
        EquipRealtime previous = previousState.value();
        boolean alarmChanged = isAlarmChanged(previous, value);
        boolean runChanged = isRunChanged(previous, value);
        boolean onlineChanged = isOnlineChanged(previous, value);

        String alarmPrev = alarmChanged ? openAlarmSegmentEventIdState.value() : null;
        String alarmNew = alarmChanged ? newSegmentEventId() : null;
        String runPrev = runChanged ? openRunSegmentEventIdState.value() : null;
        String runNew = runChanged ? newSegmentEventId() : null;
        String onlinePrev = onlineChanged ? openOnlineSegmentEventIdState.value() : null;
        String onlineNew = onlineChanged ? newSegmentEventId() : null;

        out.collect(
                new EquipRealtimeChangeEvent(
                        previous,
                        value,
                        alarmChanged,
                        runChanged,
                        onlineChanged,
                        alarmPrev,
                        alarmNew,
                        runPrev,
                        runNew,
                        onlinePrev,
                        onlineNew));

        previousState.update(value);
        if (alarmChanged) {
            openAlarmSegmentEventIdState.update(alarmNew);
        }
        if (runChanged) {
            openRunSegmentEventIdState.update(runNew);
        }
        if (onlineChanged) {
            openOnlineSegmentEventIdState.update(onlineNew);
        }
    }

    private static String newSegmentEventId() {
        return UUID.randomUUID().toString();
    }

    private boolean shouldProcess(EquipRealtime current) throws Exception {
        if (current == null) {
            return false;
        }
        EquipRealtime previous = previousState.value();
        if (previous == null) {
            return true;
        }
        // Online/offline transitions must apply even when event-time lags (Flink offline timer vs skewed device clocks).
        if (!Objects.equals(previous.getOnlineState(), current.getOnlineState())) {
            return true;
        }
        return EquipRealtimeEventTimeUtil.isNewerOrSame(current, previous);
    }

    private boolean isAlarmChanged(EquipRealtime previous, EquipRealtime current) {
        if (previous == null || current == null) {
            return false;
        }
        return !Objects.equals(previous.getAlarmState(), current.getAlarmState());
    }

    private boolean isRunChanged(EquipRealtime previous, EquipRealtime current) {
        if (previous == null || current == null) {
            return false;
        }
        return !Objects.equals(previous.getRunState(), current.getRunState());
    }

    private boolean isOnlineChanged(EquipRealtime previous, EquipRealtime current) {
        if (previous == null || current == null) {
            return false;
        }
        return !Objects.equals(previous.getOnlineState(), current.getOnlineState());
    }
}
