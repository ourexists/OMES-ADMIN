/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.EquipHealthComputeQuery;
import com.ourexists.omes.device.model.EquipHealthIndicatorDto;

import java.util.Date;
import java.util.List;

/**
 * 设备健康指标计算服务：根据报警、运行、在线记录及规则模板计算健康得分并落库
 */
public interface EquipHealthComputeService {

    /**
     * 计算指定设备在指定周期内的健康指标并保存
     *
     * @param query 包含 sn、periodStart、periodEnd、可选 templateId
     * @return 计算得到的健康指标
     */
    EquipHealthIndicatorDto computeAndSave(EquipHealthComputeQuery query);

    /**
     * 仅计算指定设备在指定周期内的健康指标，不落库（供 ForkJoin 批量计算使用）
     */
    EquipHealthIndicatorDto computeOnly(EquipHealthComputeQuery query);

    /**
     * 使用 ForkJoin 并行计算多台设备健康得分，计算完成后一次性批量写入数据库
     *
     * @param periodStart 统计周期开始
     * @param periodEnd 统计周期结束
     * @param snList 设备自编码列表
     */
    void computeBatchAndSave(Date periodStart, Date periodEnd, List<String> snList);
}
