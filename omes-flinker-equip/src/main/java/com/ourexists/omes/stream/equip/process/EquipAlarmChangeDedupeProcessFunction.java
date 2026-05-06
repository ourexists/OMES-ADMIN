package com.ourexists.omes.stream.equip.process;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.model.EquipAlarmFingerprint;
import com.ourexists.omes.stream.equip.model.EquipRealtimeChangeEvent;
import com.ourexists.omes.stream.equip.support.EquipStreamStateTtl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仅处理变化检测链路中 {@link EquipRealtimeChangeEvent#isAlarmChanged()} 为 true 的事件（属性波动、纯运行/在线不经此算子）。
 * <p>
 * 去重规则：按设备 key 维护<strong>上一条</strong>已成功下发的报警指纹（{@link EquipAlarmFingerprint#ofSn}）。
 * 当前事件报警中（{@code alarmState == 1}）时：
 * <ul>
 *   <li>当前指纹与上一条<strong>相同</strong> → 视为同一段重复报警：不写报警入库/不发通知（若同包带运行/在线变化则仍下发并去掉 alarm 段）。</li>
 *   <li>当前指纹与上一条<strong>不同</strong>（含上一条为空）→ 视为<strong>新的报警事件 / 报警有变化</strong>：在事件的 {@code alarmTexts} 末尾拼接「上一条报警」摘要（若有）与「报警产生变化」，再更新状态并下发 persist + 通知。</li>
 * </ul>
 * 报警解除（{@code alarmState == 0}）时清空「上一条」指纹与文案摘要，并原样下发 persist。
 */
public class EquipAlarmChangeDedupeProcessFunction
        extends KeyedProcessFunction<String, EquipRealtimeChangeEvent, EquipRealtimeChangeEvent> {

    private final long stateTtlMinutes;

    /** 本 key 最近一次已下发（persist+notify）的报警指纹，即「上一条」 */
    private transient ValueState<String> previousAlarmFingerprint;
    private transient ValueState<String> previousAlarmEventIdState;

    public EquipAlarmChangeDedupeProcessFunction(long stateTtlMinutes) {
        EquipStreamStateTtl.validateMinutesOption(stateTtlMinutes);
        this.stateTtlMinutes = stateTtlMinutes;
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<String> fpDesc =
                new ValueStateDescriptor<>("equip-alarm-previous-fingerprint", String.class);
        EquipStreamStateTtl.enableIfConfigured(fpDesc, stateTtlMinutes);
        previousAlarmFingerprint = getRuntimeContext().getState(fpDesc);
        ValueStateDescriptor<String> eventId =
                new ValueStateDescriptor<>("equip-alarm-previous-eventid", String.class);
        EquipStreamStateTtl.enableIfConfigured(eventId, stateTtlMinutes);
        previousAlarmEventIdState = getRuntimeContext().getState(fpDesc);
    }

    @Override
    public void processElement(EquipRealtimeChangeEvent event, Context ctx, Collector<EquipRealtimeChangeEvent> out)
            throws Exception {
        if (event == null) {
            return;
        }
        if (!event.isAlarmChanged()) {
            return;
        }
        EquipRealtime target = event.getTarget();
        if (target == null) {
            return;
        }
        if (target.getAlarmState() != null && target.getAlarmState() == 0) {
            previousAlarmFingerprint.clear();
            previousAlarmEventIdState.clear();
            EquipRealtime source = event.getSource();
            if (source != null) {
                out.collect(event);
            }
            return;
        }
        EquipRealtime source = event.getSource();
        if (source == null) {
            return;
        }
        boolean alarming = target.getAlarmState() != null && target.getAlarmState() == 1;
        if (alarming) {
            String currentFp =
                    EquipAlarmFingerprint.ofSn(target.getSelfCode(), target.getAlarmLevel(), target.getAlarmTexts());
            String previousFp = previousAlarmFingerprint.value();
            boolean sameAsPrevious = Objects.equals(currentFp, previousFp);
            if (sameAsPrevious) {
                if (event.isRunChanged() || event.isOnlineChanged()) {
                    out.collect(event.withoutAlarmPersistence());
                }
                return;
            }
            // 与上一条指纹不一致：新报警段 / 报警内容变化 — 将上一条文案与「报警产生变化」拼入 alarmTexts 再下发
            String lastEventId = previousAlarmEventIdState.value();
            EquipRealtimeChangeEvent toEmit = withAppendedAlarmChangeNotes(event, lastEventId);
            previousAlarmFingerprint.update(currentFp);
            previousAlarmEventIdState.update(joinAlarmTexts(target));
            out.collect(toEmit);
            return;
        }
        out.collect(event);
    }

    private static String joinAlarmTexts(EquipRealtime t) {
        if (t == null || t.getAlarmTexts() == null || t.getAlarmTexts().isEmpty()) {
            return "";
        }
        return t.getAlarmTexts().stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private static EquipRealtimeChangeEvent withAppendedAlarmChangeNotes(
            EquipRealtimeChangeEvent event, String lastEventId) {
        EquipRealtime original = event.getTarget();
        EquipRealtime copy = new EquipRealtime();
        try {
            BeanUtils.copyProperties(copy, original);
        } catch (Exception e) {
            throw new IllegalStateException("copy EquipRealtime for alarm text append", e);
        }
        List<String> merged = new ArrayList<>();
        if (original.getAlarmTexts() != null) {
            for (String s : original.getAlarmTexts()) {
                if (s != null && !s.isBlank()) {
                    merged.add(s.trim());
                }
            }
        }
        copy.setAlarmChangeTime(new Date());
        copy.setAlarmTexts(merged);
        return new EquipRealtimeChangeEvent(
                event.getSource(),
                copy,
                true,
                event.isRunChanged(),
                event.isOnlineChanged(),
                lastEventId,
                newSegmentEventId(),
                event.getRunPrevSegmentEventId(),
                event.getRunSegmentEventId(),
                event.getOnlinePrevSegmentEventId(),
                event.getOnlineSegmentEventId());
    }

    private static String newSegmentEventId() {
        return UUID.randomUUID().toString();
    }
}
