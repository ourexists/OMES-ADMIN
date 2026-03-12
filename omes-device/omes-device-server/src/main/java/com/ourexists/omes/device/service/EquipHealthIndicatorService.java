/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.EquipHealthIndicatorDto;
import com.ourexists.omes.device.model.EquipHealthIndicatorPageQuery;
import com.ourexists.omes.device.model.EquipHealthRuleTemplateDto;
import com.ourexists.omes.device.pojo.EquipHealthIndicator;

import java.util.Date;
import java.util.List;

/**
 * 设备健康指标服务
 */
public interface EquipHealthIndicatorService {

    /**
     * 保存或更新健康指标（同一设备同一 statTime 则更新）
     */
    void saveOrUpdate(EquipHealthIndicatorDto dto);

    /**
     * 根据设备SN与统计时间查询最新一条健康指标
     */
    EquipHealthIndicatorDto getLatestBySn(String sn, Date statTime);

    /**
     * 根据设备SN查询最新一条健康指标（任意统计时间）
     */
    EquipHealthIndicatorDto getLatestBySn(String sn);

    /**
     * 根据租户与统计时间查询该时刻所有设备的健康指标
     */
    List<EquipHealthIndicatorDto> listByStatTime(EquipHealthIndicatorPageQuery pageQuery);
}
