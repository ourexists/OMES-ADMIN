package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import com.ourexists.omes.stream.equip.support.EquipStreamStateTtl;
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
 * 设备离线：若在连续 {@link #timeoutMs} 毫秒内**链路上没有任何设备实时消息**（processing time），则输出离线态。
 * <p>
 * 状态快照仍只采纳 {@link EquipRealtimeEventTimeUtil#isNewerOrSame} 为较新的包（避免补发/乱序旧包覆盖状态），
 * 但对「更旧」的包也必须刷新离线看门狗——否则设备持续上报心跳却因时间戳/序号未推进被丢弃时，会误判沉默并周期性离线。
 * 沉默窗口用作业处理时间度量，与 {@code omes.device.offline-timeout-ms}（默认 90s）一致。
 */
public class EquipOfflineDetectProcessFunction extends KeyedProcessFunction<String, EquipRealtime, EquipRealtime> {

    private final long timeoutMs;
    private final long stateTtlMinutes;

    private transient ValueState<EquipRealtime> latestEventState;

    /** 当前注册的 processing-time 定时触发时间戳（与 register 入参一致，供 delete 使用） */
    private transient ValueState<Long> timeoutTimerState;

    /** 最近一次**收到**设备实时消息（含去重丢弃的包）时的处理时间，用于判断是否已满 {@link #timeoutMs} 沉默期 */
    private transient ValueState<Long> lastAcquireProcessingTimeState;

    public EquipOfflineDetectProcessFunction(long timeoutMs, long stateTtlMinutes) {
        if (timeoutMs <= 0L) {
            throw new IllegalArgumentException("offlineTimeoutMs must be positive, was: " + timeoutMs);
        }
        EquipStreamStateTtl.validateMinutesOption(stateTtlMinutes);
        this.timeoutMs = timeoutMs;
        this.stateTtlMinutes = stateTtlMinutes;
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<EquipRealtime> latestDesc = new ValueStateDescriptor<>("latest-event-state", EquipRealtime.class);
        EquipStreamStateTtl.enableIfConfigured(latestDesc, stateTtlMinutes);
        ValueStateDescriptor<Long> timerDesc = new ValueStateDescriptor<>("timeout-timer-state", Long.class);
        EquipStreamStateTtl.enableIfConfigured(timerDesc, stateTtlMinutes);
        ValueStateDescriptor<Long> acquireDesc =
                new ValueStateDescriptor<>("offline-last-accepted-processing-time", Long.class);
        EquipStreamStateTtl.enableIfConfigured(acquireDesc, stateTtlMinutes);
        latestEventState = getRuntimeContext().getState(latestDesc);
        timeoutTimerState = getRuntimeContext().getState(timerDesc);
        lastAcquireProcessingTimeState = getRuntimeContext().getState(acquireDesc);
    }

    @Override
    public void processElement(EquipRealtime value, Context ctx, Collector<EquipRealtime> out) throws Exception {
        long now = ctx.timerService().currentProcessingTime();
        EquipRealtime current = latestEventState.value();
        // 不比当前状态新：不覆盖快照，但仍视为链路上有流量（并刷新 state TTL，避免仅 TTL 与去重叠加导致异常）
        if (current != null && !EquipRealtimeEventTimeUtil.isNewerOrSame(value, current)) {
            latestEventState.update(current);
            scheduleOfflineWatchdog(ctx, now);
            return;
        }
        latestEventState.update(value);
        scheduleOfflineWatchdog(ctx, now);
    }

    /** 收到任意实时包时调用：更新沉默计时起点并重注册 processing-time 定时器 */
    private void scheduleOfflineWatchdog(Context ctx, long processingTimeNow) throws Exception {
        lastAcquireProcessingTimeState.update(processingTimeNow);
        long deadline = Math.addExact(processingTimeNow, timeoutMs);
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
            ctx.timerService().deleteProcessingTimeTimer(timestamp);
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
