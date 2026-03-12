/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.EquipHealthRuleTemplateDto;
import com.ourexists.omes.device.pojo.EquipHealthRuleTemplate;

import java.util.List;

/**
 * 设备健康规则模板服务
 */
public interface EquipHealthRuleTemplateService {

    /**
     * 获取默认模板（内置默认规则，不查库）
     */
    EquipHealthRuleTemplateDto getDefaultTemplate();

    /**
     * 根据ID获取模板，不存在则返回默认
     */
    EquipHealthRuleTemplateDto getByIdOrDefault(String templateId);

    /**
     * 保存或更新模板
     */
    void saveOrUpdate(EquipHealthRuleTemplateDto dto);

    /**
     * 查询所有模板列表
     */
    List<EquipHealthRuleTemplateDto> list();
}
