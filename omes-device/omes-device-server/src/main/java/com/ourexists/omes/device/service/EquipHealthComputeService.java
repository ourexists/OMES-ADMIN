/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.EquipHealthComputeQuery;
import com.ourexists.omes.device.model.EquipHealthIndicatorDto;

/**
 * 设备健康指标计算服务：根据报警、运行、在线记录及规则模板计算健康得分并落库
 */
public interface EquipHealthComputeService {

    /**
     * 计算指定设备在指定周期内的健康指标并保存
     *
     * @param query 包含 sn、periodStart、periodEnd、可选 templateId
     * @param equipId 设备ID（可为空，会从实时缓存取）
     * @param tenantId 租户ID（可为空，会从实时缓存取）
     * @return 计算得到的健康指标
     */
    EquipHealthIndicatorDto computeAndSave(EquipHealthComputeQuery query, String equipId);
}
