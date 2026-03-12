/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.feign.EquipHealthFeign;
import com.ourexists.omes.device.model.EquipHealthComputeQuery;
import com.ourexists.omes.ucenter.feign.TenantFeign;
import com.ourexists.omes.ucenter.tenant.TenantVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 设备健康指标定时任务：按周期（默认每小时）为所有设备生成健康指标
 */
@Slf4j
@Component
public class EquipHealthScheduleJob {

    /** 统计周期小时数，与默认模板一致 */
    private static final int PERIOD_HOURS = 24;

    @Autowired
    private EquipHealthFeign equipHealthFeign;
    @Autowired
    private EquipRealtimeManager equipRealtimeManager;
    @Autowired
    private TenantFeign tenantFeign;

    /**
     * 每小时整点执行一次，统计过去 24 小时的设备健康
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateHealthIndicators() {
        UserContext.defaultTenant();
        UserContext.getTenant().setSkipMain(false);
        try {
            List<TenantVo> tenantVos = RemoteHandleUtils.getDataFormResponse(tenantFeign.all());
            for (TenantVo tenantVo : tenantVos) {
                UserContext.getTenant().setTenantId(tenantVo.getTenantCode());
                Map<String, EquipRealtime> all = equipRealtimeManager.getAll();
                Date periodEnd = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(periodEnd);
                cal.add(Calendar.HOUR_OF_DAY, -PERIOD_HOURS);
                Date periodStart = cal.getTime();

                for (EquipRealtime rt : all.values()) {
                    if (!tenantVo.getTenantCode().equals(rt.getTenantId())) {
                        continue;
                    }
                    try {
                        EquipHealthComputeQuery query = new EquipHealthComputeQuery();
                        query.setSn(rt.getSelfCode());
                        query.setPeriodStart(periodStart);
                        query.setPeriodEnd(periodEnd);
                        RemoteHandleUtils.getDataFormResponse(equipHealthFeign.computeAndSave(query));
                    } catch (EraCommonException e) {
                        log.warn("equip health compute failed, sn={}, msg={}", rt.getSelfCode(), e.getMessage());
                    }
                }
            }
        } catch (EraCommonException e) {
            log.error("EquipHealthScheduleJob error: {}", e.getMessage(), e);
        }
    }
}
