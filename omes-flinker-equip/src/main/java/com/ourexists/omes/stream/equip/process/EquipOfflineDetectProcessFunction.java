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
 * 工业级设备离线检测（Processing Time）
 *
 * 核心机制：
 * 1. 仅记录 lastSeenProcessingTime
 * 2. 每次有效数据 → 重新注册 timer(lastSeen + timeout)
 * 3. timer 触发时只做“是否仍为最新 deadline”判断
 * 4. 不允许旧数据刷新在线状态（防“续命”）
 */
public class EquipOfflineDetectProcessFunction
        extends KeyedProcessFunction<String, EquipRealtime, EquipRealtime> {

    private final long timeoutMs;
    private final long stateTtlMinutes;

    private transient ValueState<EquipRealtime> latestState;
    private transient ValueState<Long> lastSeenProcessingTime;

    public EquipOfflineDetectProcessFunction(long timeoutMs, long stateTtlMinutes) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        EquipStreamStateTtl.validateMinutesOption(stateTtlMinutes);
        this.timeoutMs = timeoutMs;
        this.stateTtlMinutes = stateTtlMinutes;
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<EquipRealtime> latestDesc =
                new ValueStateDescriptor<>("latest-state", EquipRealtime.class);
        EquipStreamStateTtl.enableIfConfigured(latestDesc, stateTtlMinutes);

        ValueStateDescriptor<Long> seenDesc =
                new ValueStateDescriptor<>("last-seen-time", Long.class);
        EquipStreamStateTtl.enableIfConfigured(seenDesc, stateTtlMinutes);

        latestState = getRuntimeContext().getState(latestDesc);
        lastSeenProcessingTime = getRuntimeContext().getState(seenDesc);
    }

    @Override
    public void processElement(EquipRealtime value,
                               Context ctx,
                               Collector<EquipRealtime> out) throws Exception {

        long now = ctx.timerService().currentProcessingTime();

        EquipRealtime current = latestState.value();

        // ❗ 关键1：只接受“新数据”，旧数据直接丢弃（不刷新在线）
        if (current != null && !EquipRealtimeEventTimeUtil.isNewerOrSame(value, current)) {
            return;
        }

        // 更新最新状态
        latestState.update(value);

        // 更新最后一次“有效接收时间”
        lastSeenProcessingTime.update(now);

        // 注册新的离线检测 timer
        ctx.timerService().registerProcessingTimeTimer(now + timeoutMs);
    }

    @Override
    public void onTimer(long timestamp,
                        OnTimerContext ctx,
                        Collector<EquipRealtime> out) throws Exception {

        EquipRealtime latest = latestState.value();
        Long lastSeen = lastSeenProcessingTime.value();

        if (latest == null || lastSeen == null) {
            return;
        }

        // ❗ 关键2：防止旧 timer 或重复 timer 误触发
        if (timestamp != lastSeen + timeoutMs) {
            return;
        }

        long now = ctx.timerService().currentProcessingTime();

        // 二次校验（防极端乱序/延迟）
        if (now - lastSeen < timeoutMs) {
            return;
        }

        String key = ctx.getCurrentKey();
        if (StringUtils.isBlank(key)) {
            return;
        }

        EquipRealtime offline = copy(latest);
        offline.setSelfCode(key);
        offline.offline();
        offline.setTime(new Date(now));

        out.collect(offline);
    }

    /**
     * 深拷贝，避免状态污染
     */
    private static EquipRealtime copy(EquipRealtime src) {
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
        return src == null ? null : new ArrayList<>(src);
    }
}