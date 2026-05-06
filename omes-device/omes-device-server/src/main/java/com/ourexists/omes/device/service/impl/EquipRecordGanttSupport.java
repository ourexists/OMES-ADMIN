/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.omes.device.model.EquipRecordAlarmVo;
import com.ourexists.omes.device.model.EquipRecordOnlineVo;
import com.ourexists.omes.device.model.EquipRecordRunVo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 将 countMerging 结果整理为适合 ECharts 时间轴甘特条（custom rect）的数据：
 * 按开始时间排序、裁剪到查询窗口、闭合未结束区间、合并相邻或重叠的同状态段。
 */
public final class EquipRecordGanttSupport {

    private EquipRecordGanttSupport() {
    }

    public static void normalizeOnlineForGantt(List<EquipRecordOnlineVo> segments, Date windowStart, Date windowEnd) {
        normalizeSegmentsForGantt(segments, windowStart, windowEnd,
                EquipRecordOnlineVo::getStartTime,
                EquipRecordOnlineVo::setStartTime,
                EquipRecordOnlineVo::getEndTime,
                EquipRecordOnlineVo::setEndTime,
                EquipRecordOnlineVo::getState,
                Comparator.comparing(EquipRecordOnlineVo::getStartTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(EquipRecordOnlineVo::getId, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    public static void normalizeRunForGantt(List<EquipRecordRunVo> segments, Date windowStart, Date windowEnd) {
        normalizeSegmentsForGantt(segments, windowStart, windowEnd,
                EquipRecordRunVo::getStartTime,
                EquipRecordRunVo::setStartTime,
                EquipRecordRunVo::getEndTime,
                EquipRecordRunVo::setEndTime,
                EquipRecordRunVo::getState,
                Comparator.comparing(EquipRecordRunVo::getStartTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(EquipRecordRunVo::getId, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    public static void normalizeAlarmForGantt(List<EquipRecordAlarmVo> segments, Date windowStart, Date windowEnd) {
        normalizeSegmentsForGantt(segments, windowStart, windowEnd,
                EquipRecordAlarmVo::getStartTime,
                EquipRecordAlarmVo::setStartTime,
                EquipRecordAlarmVo::getEndTime,
                EquipRecordAlarmVo::setEndTime,
                EquipRecordAlarmVo::getState,
                Comparator.comparing(EquipRecordAlarmVo::getStartTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(EquipRecordAlarmVo::getId, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private static <T> void normalizeSegmentsForGantt(List<T> segments, Date windowStart, Date windowEnd,
            Function<T, Date> getStart,
            BiConsumer<T, Date> setStart,
            Function<T, Date> getEnd,
            BiConsumer<T, Date> setEnd,
            Function<T, Integer> getState,
            Comparator<T> sortKey) {
        if (CollectionUtil.isBlank(segments) || windowStart == null || windowEnd == null) {
            return;
        }
        for (T vo : segments) {
            Date st = getStart.apply(vo);
            Date en = getEnd.apply(vo);
            if (st != null && st.before(windowStart)) {
                setStart.accept(vo, windowStart);
            }
            if (en != null && en.after(windowEnd)) {
                setEnd.accept(vo, windowEnd);
            }
        }
        segments.sort(sortKey);
        int n = segments.size();
        for (int i = 0; i < n - 1; i++) {
            T cur = segments.get(i);
            if (getEnd.apply(cur) != null) {
                continue;
            }
            Date nextStart = getStart.apply(segments.get(i + 1));
            if (nextStart != null) {
                setEnd.accept(cur, nextStart.before(windowEnd) ? nextStart : windowEnd);
            } else {
                setEnd.accept(cur, windowEnd);
            }
        }
        if (n > 0) {
            T last = segments.get(n - 1);
            if (getEnd.apply(last) == null) {
                setEnd.accept(last, windowEnd);
            }
        }
        List<T> merged = new ArrayList<>();
        for (T vo : segments) {
            Date st = getStart.apply(vo);
            Date en = getEnd.apply(vo);
            if (st == null || en == null || !st.before(en)) {
                continue;
            }
            if (merged.isEmpty()) {
                merged.add(vo);
                continue;
            }
            T prev = merged.get(merged.size() - 1);
            Date prevEnd = getEnd.apply(prev);
            if (Objects.equals(getState.apply(prev), getState.apply(vo))
                    && prevEnd != null
                    && !prevEnd.before(st)) {
                if (en.after(prevEnd)) {
                    setEnd.accept(prev, en);
                }
            } else {
                merged.add(vo);
            }
        }
        segments.clear();
        segments.addAll(merged);
    }
}
