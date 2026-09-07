/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.jdbc.EquipCollectBucketStatRow;
import com.ourexists.omes.device.mapper.EquipCollectMapper;
import com.ourexists.omes.device.model.EquipCollectDto;
import com.ourexists.omes.device.model.EquipCollectPageQuery;
import com.ourexists.omes.device.pojo.EquipCollect;
import com.ourexists.omes.device.service.EquipCollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
public class EquipCollectServiceImpl extends AbstractMyBatisPlusService<EquipCollectMapper, EquipCollect>
        implements EquipCollectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<EquipCollect> entityList, int batchSize) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        List<EquipCollect> all = new ArrayList<>(entityList);
        int size = all.size();
        int chunk = Math.max(batchSize, 1);
        for (int i = 0; i < size; i += chunk) {
            int end = Math.min(i + chunk, size);
            List<EquipCollect> part = all.subList(i, end);
            this.baseMapper.insertBatchWithJsonb(part);
        }
        return true;
    }

    @Override
    public Page<EquipCollect> selectByPage(EquipCollectPageQuery dto) {
        LambdaQueryWrapper<EquipCollect> base = new LambdaQueryWrapper<EquipCollect>()
                .eq(StringUtils.hasText(dto.getSn()), EquipCollect::getSn, dto.getSn())
                .between(dto.getStartDate() != null && dto.getEndDate() != null, EquipCollect::getTime, dto.getStartDate(), dto.getEndDate());
        String interval = dto.getAggregateInterval();
        if (!StringUtils.hasText(interval) || "RAW".equalsIgnoreCase(interval.trim())) {
            return this.page(new Page<>(dto.getPage(), dto.getPageSize()),
                    base.orderByAsc(EquipCollect::getId));
        }
        String raw = interval.trim().toUpperCase(Locale.ROOT);
        boolean chartAutoLast = "AUTO".equals(raw);
        String bucketMode;
        if (chartAutoLast) {
            bucketMode = resolveAutoBucketMode(dto.getStartDate(), dto.getEndDate());
        } else {
            bucketMode = raw;
            if (!isSupportedStatsAggregateMode(bucketMode)) {
                return this.page(new Page<>(dto.getPage(), dto.getPageSize()),
                        base.orderByAsc(EquipCollect::getId));
            }
        }
        List<EquipCollect> aggregated = chartAutoLast
                ? this.baseMapper.selectLastPerBucket(dto.getSn(), dto.getStartDate(), dto.getEndDate(), bucketMode, null, null)
                : foldStatsFlat(this.baseMapper.selectStatsAggregateFlat(dto.getSn(), dto.getStartDate(), dto.getEndDate(), bucketMode), dto.getSn());
        if (chartAutoLast) {
            long n = aggregated.size();
            Page<EquipCollect> out = new Page<>(1, n == 0 ? 1 : n, n);
            out.setRecords(aggregated);
            return out;
        }
        long total = aggregated.size();
        long page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        long pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        int from = (int) ((page - 1) * pageSize);
        List<EquipCollect> slice;
        if (from >= total || total == 0) {
            slice = Collections.emptyList();
        } else {
            int to = (int) Math.min(from + pageSize, total);
            slice = aggregated.subList(from, to);
        }
        Page<EquipCollect> out = new Page<>(page, pageSize, total);
        out.setRecords(slice);
        return out;
    }

    /**
     * 趋势图：按查询跨度自动选桶，避免点过密或过稀。
     */
    private static String resolveAutoBucketMode(Date start, Date end) {
        if (start == null || end == null) {
            return "HOUR";
        }
        long span = end.getTime() - start.getTime();
        if (span <= 0L) {
            return "HOUR";
        }
        long hours = span / 3_600_000L;
        long days = span / 86_400_000L;
        if (hours <= 48L) {
            return "MIN30";
        }
        if (days <= 14L) {
            return "HOUR";
        }
        if (days <= 400L) {
            return "DAY";
        }
        return "MONTH";
    }

    /**
     * 将 PostgreSQL 分组统计扁平行折叠为按桶一条 {@link EquipCollect}（data 中带 _avg/_min/_max/_count）。
     */
    private static List<EquipCollect> foldStatsFlat(List<EquipCollectBucketStatRow> flat, String sn) {
        if (CollectionUtils.isEmpty(flat)) {
            return Collections.emptyList();
        }
        LinkedHashMap<Long, EquipCollect> byBucketMs = new LinkedHashMap<>();
        for (EquipCollectBucketStatRow r : flat) {
            if (r.getBucketStart() == null || !StringUtils.hasText(r.getAttrKey())) {
                continue;
            }
            long ms = r.getBucketStart().getTime();
            EquipCollect ec = byBucketMs.computeIfAbsent(ms, k -> {
                EquipCollect e = new EquipCollect();
                e.setSn(sn);
                e.setTime(r.getBucketStart());
                e.setData(new LinkedHashMap<>());
                return e;
            });
            String base = r.getAttrKey();
            ec.getData().put(base + "_avg", fmt(r.getAvgVal()));
            ec.getData().put(base + "_min", fmt(r.getMinVal()));
            ec.getData().put(base + "_max", fmt(r.getMaxVal()));
            ec.getData().put(base + "_count", r.getCnt() == null ? "0" : String.valueOf(r.getCnt()));
        }
        return new ArrayList<>(byBucketMs.values());
    }

    private static boolean isSupportedStatsAggregateMode(String mode) {
        return "MIN30".equals(mode) || "HOUR".equals(mode) || "DAY".equals(mode) || "MONTH".equals(mode);
    }

    private static String fmt(Double v) {
        if (v == null || Double.isNaN(v) || Double.isInfinite(v)) {
            return "";
        }
        return fmt(v.doubleValue());
    }

    private static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "";
        }
        return BigDecimal.valueOf(v).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    @Override
    public void addOrUpdate(EquipCollectDto dto) {
        saveOrUpdate(EquipCollect.wrap(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeBatchByIds(ids);
    }

    @Override
    public List<EquipCollect> queryByEquip(List<String> sns) {
        return this.list(new LambdaUpdateWrapper<EquipCollect>().in(EquipCollect::getSn, sns));
    }

    @Override
    public EquipCollect queryByEquip(String sn) {
        return this.getOne(new LambdaUpdateWrapper<EquipCollect>().eq(EquipCollect::getSn, sn));
    }
}
