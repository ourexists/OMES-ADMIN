package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备离线：以「成功获取并采纳的设备实时数据」为一次刷新；若在连续 {@link #timeoutMs} 毫秒内没有任何新的采纳数据，则输出离线态。
 * <p>
 * 「获取」指 Flink 作业从队列收到一条 {@link EquipRealtime} 且通过 {@link EquipRealtimeEventTimeUtil#isNewerOrSame} 判定为不比当前状态更旧
 * （避免补发/乱序旧包误刷新）。沉默窗口用作业处理时间度量，与 {@code omes.device.offline-timeout-ms}（默认 90s）一致。
 */
public class EquipOfflineDetectProcessFunction extends KeyedProcessFunction<String, EquipRealtime, EquipRealtime> {

    private final long timeoutMs;

    private transient ValueState<EquipRealtime> latestEventState;

    /** 当前注册的 processing-time 定时触发时间戳（与 register 入参一致，供 delete 使用） */
    private transient ValueState<Long> timeoutTimerState;

    /** 最近一次「采纳」的设备数据对应的处理时间，用于判断自上次获取起是否已满 timeoutMs */
    private transient ValueState<Long> lastAcquireProcessingTimeState;

    public EquipOfflineDetectProcessFunction(long timeoutMs) {
        if (timeoutMs <= 0L) {
            throw new IllegalArgumentException("offlineTimeoutMs must be positive, was: " + timeoutMs);
        }
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void open(Configuration parameters) {
        latestEventState = getRuntimeContext().getState(new ValueStateDescriptor<>("latest-event-state", EquipRealtime.class));
        timeoutTimerState = getRuntimeContext().getState(new ValueStateDescriptor<>("timeout-timer-state", Long.class));
        lastAcquireProcessingTimeState =
                getRuntimeContext().getState(new ValueStateDescriptor<>("offline-last-accepted-processing-time", Long.class));
    }

    @Override
    public void processElement(EquipRealtime value, Context ctx, Collector<EquipRealtime> out) throws Exception {
        EquipRealtime current = latestEventState.value();
        if (current != null && !EquipRealtimeEventTimeUtil.isNewerOrSame(value, current)) {
            return;
        }
        latestEventState.update(value);
        long now = ctx.timerService().currentProcessingTime();
        lastAcquireProcessingTimeState.update(now);
        long deadline = Math.addExact(now, timeoutMs);
        Long oldTimer = timeoutTimerState.value();
        if (oldTimer != null && oldTimer != deadline) {
            ctx.timerService().deleteProcessingTimeTimer(oldTimer);
        }
        if (oldTimer == null || oldTimer != deadline) {
            ctx.timerService().registerProcessingTimeTimer(deadline);
        }
        timeoutTimerState.update(deadline);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<EquipRealtime> out) throws Exception {
        EquipRealtime latestEvent = latestEventState.value();
        if (latestEvent == null) {
            return;
        }
        long now = ctx.timerService().currentProcessingTime();
        Long lastAcquire = lastAcquireProcessingTimeState.value();
        if (lastAcquire == null) {
            lastAcquireProcessingTimeState.update(now);
            rescheduleOfflineTimer(Math.addExact(now, timeoutMs), ctx);
            return;
        }
        if (now - lastAcquire < timeoutMs) {
            rescheduleOfflineTimer(Math.addExact(lastAcquire, timeoutMs), ctx);
            return;
        }
        Long timer = timeoutTimerState.value();
        if (timer == null || timestamp < timer) {
            return;
        }
        String currentKey = ctx.getCurrentKey();
        if (StringUtils.isBlank(currentKey)) {
            return;
        }
        EquipRealtime offlineTarget = copyStateSnapshotForEmit(latestEvent);
        offlineTarget.setSelfCode(currentKey);
        offlineTarget.offline();
        offlineTarget.setTime(new Date(timestamp));
        out.collect(offlineTarget);
        timeoutTimerState.clear();
    }

    private void rescheduleOfflineTimer(long deadline, Context ctx) throws Exception {
        Long cur = timeoutTimerState.value();
        if (cur != null && cur != deadline) {
            ctx.timerService().deleteProcessingTimeTimer(cur);
        }
        if (cur == null || cur != deadline) {
            ctx.timerService().registerProcessingTimeTimer(deadline);
        }
        timeoutTimerState.update(deadline);
    }

    /**
     * 与状态中对象分离：列表单独 new，避免 {@link EquipRealtime#offline()} 或下游改写集合时污染 {@link #latestEventState}。
     */
    private static EquipRealtime copyStateSnapshotForEmit(EquipRealtime src) {
        EquipRealtime dst = new EquipRealtime();
        dst.setId(src.getId());
        dst.setName(src.getName());
        dst.setSelfCode(src.getSelfCode());
        dst.setEquipRealtimeConfig(src.getEquipRealtimeConfig());
        dst.setOnlineState(src.getOnlineState());
        dst.setRunState(src.getRunState());
        dst.setAlarmState(src.getAlarmState());
        dst.setEquipAttrRealtimes(copyList(src.getEquipAttrRealtimes()));
        dst.setEquipControlRealtimes(copyList(src.getEquipControlRealtimes()));
        dst.setAlarmTexts(copyList(src.getAlarmTexts()));
        dst.setAlarmLevel(src.getAlarmLevel());
        dst.setTenantId(src.getTenantId());
        dst.setWorkshopCode(src.getWorkshopCode());
        dst.setTime(src.getTime());
        dst.setOnlineChangeTime(src.getOnlineChangeTime());
        dst.setRunChangeTime(src.getRunChangeTime());
        dst.setAlarmChangeTime(src.getAlarmChangeTime());
        return dst;
    }

    private static <T> List<T> copyList(List<T> src) {
        if (src == null) {
            return null;
        }
        return new ArrayList<>(src);
    }
}
